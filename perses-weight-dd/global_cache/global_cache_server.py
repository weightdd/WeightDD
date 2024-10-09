import multiprocessing as mp
import socket
from typing import Mapping, Union
import logging
import atexit
import json

DEFAULT_PORT = 10000
DEFAULT_MAX_PORT = 10050
SHA512_DIGEST_LENGTH = 128

class NullServer:
    def start():
        pass

    def save_database(self, path_to_save: str) -> None:
        pass

    def get_saved_time(self):
        return 0

    def reset_saved_time(self) -> None:
        pass

    def get_socket_port_number(self):
        return None


class GlobalCacheServer:
    def __init__(self, database: Mapping[str, Mapping[str, Union[float, int]]]) -> None:
        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.database = mp.Manager().dict(database)
        self.saved_time = mp.Value("f", 0.0)
        port = DEFAULT_PORT
        # socket cannot be close immediately, so backup ports are needed if
        # benchmarks are run consecutively.
        while port - DEFAULT_PORT < DEFAULT_MAX_PORT:
            try:
                self.server_socket.bind(("127.0.0.1", port))
                break
            except OSError:
                logging.debug("port: %s is not available.", port)
                port += 1
                continue
        self.server_socket.listen(5)

        self.worker = mp.Process(target=self.handle_request, args=())
        self.worker.daemon = True

        def cleanup() -> None:
            self.server_socket.close()
            self.worker.terminate()

        atexit.register(cleanup)

    def start(self) -> None:
        self.worker.start()
        logging.debug("server started")

    def get_saved_time(self) -> float:
        return self.saved_time.value

    def get_socket_port_number(self) -> int:
        return self.server_socket.getsockname()[1]

    def reset_saved_time(self) -> None:
        self.saved_time.value = 0

    def save_database(self, path_to_save: str) -> None:
        with open(path_to_save, 'w') as f:
            json.dump(dict(self.database), f)

    def handle_request(self) -> None:
        while True:
            client, _ = self.server_socket.accept()
            logging.debug("a client connected")
            mode = client.recv(2).decode().strip()
            # query
            if mode == "q":
                logging.debug("received query mode selection")
                self.__handle_query(client)
            # update
            elif mode == "u":
                logging.debug("received update mode selection")
                self.__handle_update(client)
            # ping
            elif mode == "p":
                logging.debug("server is pinged")
                client.send(b"PONG")
            else:
                logging.error("received invalid mode selection %s", mode)
            client.close()

    def __handle_query(self, client: socket.socket) -> None:
        sha512 = client.recv(SHA512_DIGEST_LENGTH + 1).decode().strip()
        if len(sha512) != SHA512_DIGEST_LENGTH:
            logging.error("received an invalid query request: %s", sha512)
            return
        if sha512 in self.database.keys():
            result = self.database[sha512]
            exit_code = result["exitcode"]
            execution_time = result["time"]
            client.send((str(exit_code)).encode())
            self.saved_time.value += execution_time
            logging.debug(
                "cache hit: exit_code = %s, execution_time = %s",
                exit_code,
                execution_time,
            )
        else:
            client.send(b"cache miss")
            logging.debug("cache miss")

    def __handle_update(self, client: socket.socket) -> None:
        sha512 = client.recv(SHA512_DIGEST_LENGTH + 1).decode().strip()
        if len(sha512) != SHA512_DIGEST_LENGTH:
            logging.error("received an invalid update request: %s", sha512)
            return
        try:
            exit_code = int(client.recv(2).decode().strip())
            execution_time = float(client.recv(20).decode().strip())
        except ValueError as e:
            logging.error("received invalid data for updating: %s", e)
            return
        self.database[sha512] = {"exitcode": exit_code, "time": execution_time}
        logging.debug(
            "updated: exit_code = %s, execution_time = %s", exit_code, execution_time
        )


