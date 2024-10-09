import unittest
import socket
from typing import Tuple
from global_cache_server import GlobalCacheServer


class GlobalCacheServerTest(unittest.TestCase):
    def test_ping(self):
        database = dict()
        my_server = GlobalCacheServer(database)
        my_server.start()
        response = self.__send_via_socket(
            "p\n".encode(), my_server.server_socket.getsockname()
        )
        self.assertEqual(response, "PONG")

    def test_query(self):
        database = {
            "db883b2d3f815c5026d6b05ae650f9d303645b1e0cc487328f8f09ff60b72f54"
            + "e77fb3cc341b453e1059466c900504d9c361352b5af38412ec7d9467de506889": {
                "time": 0.01,
                "exitcode": 1,
            }
        }
        my_server = GlobalCacheServer(database)
        my_server.start()
        response = self.__send_via_socket(
            (
                "q\n"
                + "db883b2d3f815c5026d6b05ae650f9d303645b1e0cc487328f8f09ff60b72f54"
                + "e77fb3cc341b453e1059466c900504d9c361352b5af38412ec7d9467de506889\n"
            ).encode(),
            my_server.server_socket.getsockname(),
        )
        places = 6
        self.assertEqual(response, "1")
        self.assertAlmostEqual(my_server.saved_time.value, 0.01, places)

    def test_update(self):
        database = dict()
        my_server = GlobalCacheServer(database)
        my_server.start()
        response = self.__send_via_socket(
            (
                "u\n"
                + "db883b2d3f815c5026d6b05ae650f9d303645b1e0cc487328f8f09ff60b72f54"
                + "e77fb3cc341b453e1059466c900504d9c361352b5af38412ec7d9467de506889\n"
                + "1\n"
                + "0.0012\n"
            ).encode(),
            my_server.server_socket.getsockname(),
        )
        places = 6
        update_database = my_server.database
        self.assertEqual(response, "")
        self.assertEqual(
            update_database.keys(),
            [
                "db883b2d3f815c5026d6b05ae650f9d303645b1e0cc487328f8f09ff60b72f54"
                + "e77fb3cc341b453e1059466c900504d9c361352b5af38412ec7d9467de506889"
            ],
        )
        self.assertAlmostEqual(update_database.values()[0]["time"], 0.0012, places)
        self.assertEqual(update_database.values()[0]["exitcode"], 1)

    def __send_via_socket(
        self,
        msg: bytes,
        sockname: Tuple[str, int],
    ) -> str:
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_socket.connect(sockname)
        client_socket.sendall(msg)

        response = client_socket.recv(1024)
        client_socket.close()
        return response.decode()


if __name__ == "__main__":
    unittest.main()
