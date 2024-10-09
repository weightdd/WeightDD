/*
 * Copyright (C) 2018-2024 University of Waterloo.
 *
 * This file is part of Perses.
 *
 * Perses is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3, or (at your option) any later version.
 *
 * Perses is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Perses; see the file LICENSE.  If not see <http://www.gnu.org/licenses/>.
 */
package org.perses.globalcache

import com.google.common.flogger.FluentLogger
import com.google.common.hash.HashCode
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.perses.reduction.TestScriptHistory
import org.perses.util.DaemonThreadPool
import org.perses.util.ktWarning
import java.net.InetSocketAddress
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

class CacheServer(pathToFile: Path) : AutoCloseable {
  var port: Int
    private set
  private var group = NioEventLoopGroup(MAX_THREAD_NUM)
  private val history = TestScriptHistory.loadFromCSV(pathToFile)
  private val savedTimeInMillie: AtomicInteger

  init {
    port = DEFAULT_PORT
    savedTimeInMillie = AtomicInteger(0)
  }

  fun start() {
    val startUpReadyLatch = CountDownLatch(1)
    DaemonThreadPool.createSingleThreadPool().execute {
      while (true) {
        try {
          val serverBootStrap = ServerBootstrap()
          serverBootStrap.group(group)
            .channel(NioServerSocketChannel::class.java)
            .localAddress(InetSocketAddress(port))
            .childHandler(
              object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(socketChannel: SocketChannel) {
                  socketChannel
                    .pipeline()
                    .addLast(CacheServerHandler(history, savedTimeInMillie))
                }
              },
            )
          val channelFuture = serverBootStrap.bind().sync()
          startUpReadyLatch.countDown()
          channelFuture.channel().closeFuture().sync()
          break
        } catch (e: Exception) {
          if (port - DEFAULT_PORT > DEFAULT_MAX_PORT) {
            logger.ktWarning { "Failed to start server. Maximum retries reached." }
            break
          }
          logger.ktWarning { "Port $port is already in use. Trying the next port." }
          ++port
        }
      }
    }
    startUpReadyLatch.await()
  }

  override fun close() {
    group.shutdownGracefully()
  }

  val savedTimeInMillies: Int
    get() = savedTimeInMillie.get()

  val database: Map<HashCode, TestScriptHistory.Result>
    get() = history.asReadOnlyMap()

  fun saveDatabase(pathToSave: Path) {
    // Save database to file
    history.saveToCSV(pathToSave)
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()
    private const val MAX_THREAD_NUM = 5
    private const val DEFAULT_PORT = 10000
    private const val DEFAULT_MAX_PORT = 10150
  }
}
