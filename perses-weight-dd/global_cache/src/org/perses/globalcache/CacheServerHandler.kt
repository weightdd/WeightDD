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

import com.google.common.collect.ImmutableList
import com.google.common.flogger.FluentLogger
import com.google.common.hash.HashCode
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.CharsetUtil
import org.perses.reduction.TestScriptHistory
import org.perses.reduction.TestScriptHistory.Result
import org.perses.util.ktInfo
import org.perses.util.shell.ExitCode
import org.perses.util.toImmutableList
import java.util.concurrent.atomic.AtomicInteger

@Sharable
class CacheServerHandler(
  private val history: TestScriptHistory,
  private val savedTimeInMillie: AtomicInteger,
) : ChannelInboundHandlerAdapter() {
  override fun channelRead(context: ChannelHandlerContext, message: Any) {
    val messageStr = (message as ByteBuf).toString(CharsetUtil.UTF_8)
    logger.ktInfo { "Received message: $messageStr" }
    when {
      messageStr.startsWith(REQUEST_MODE_PING) -> {
        context.writeAndFlush(Unpooled.copiedBuffer(RESPONSE_MODE_PING, CharsetUtil.UTF_8))
      }
      messageStr.startsWith(REQUEST_MODE_QUERY) -> {
        handleQuery(context, messageStr.substring(2))
      }
      messageStr.startsWith(REQUEST_MODE_UPDATE) -> {
        handleUpdate(context, messageStr.substring(2))
      }
      else -> {
        logger.ktInfo { "Invalid message: $messageStr" }
      }
    }
  }

  private fun handleUpdate(context: ChannelHandlerContext, message: String) {
    // Handle update request
    val request = parseRequest(message)
    if (request.size == 3) {
      val sha512 = HashCode.fromString(request[0])
      val exitCode = request[1].toInt()
      val time = request[2].toInt()
      history.cacheExecutionHistory(sha512, Result(ExitCode(exitCode), time))
      savedTimeInMillie.getAndAdd(time)
      logger.atInfo().log("cache updated: exit_code = %d, execution_time = %d", exitCode, time)
      // Note that the 'sendMessage' should be called in the end, so that unit test can get accurate reading of the saved time.
      sendMessage(context, RESPONSE_MODE_UPDATE_SUCCESS)
    } else {
      error("Unhandled request: $message")
    }
  }

  private fun handleQuery(ctx: ChannelHandlerContext, msg: String) {
    // Handle query request
    val request = parseRequest(msg)
    val sha512 = HashCode.fromString(request.single())
    val result = history.getExecutionHistoryFor(sha512)
    if (result != null) {
      val exitCode = result.exitCode.intValue.toString()
      val time = result.ellapsedMillies
      savedTimeInMillie.addAndGet(time)
      logger.ktInfo { "cache hit: exit_code = $exitCode, execution_time = $time" }
      // Note that the 'sendMessage' should be called in the end, so that unit test can get accurate reading of the saved time.
      sendMessage(ctx, exitCode)
    } else {
      logger.ktInfo { "cache miss" }
      sendMessage(ctx, RESPONSE_MODE_QUERY_MISS)
    }
  }

  private fun sendMessage(ctx: ChannelHandlerContext, messageBuilder: String) {
    ctx.writeAndFlush(Unpooled.copiedBuffer(messageBuilder, CharsetUtil.UTF_8))
  }

  private fun parseRequest(message: String): ImmutableList<String> {
    return message.split(REQUEST_STRING_SPLITTER).dropLastWhile { it.isEmpty() }.toImmutableList()
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()
    private const val REQUEST_MODE_PING = "p"
    private const val RESPONSE_MODE_PING = "Pong"
    private const val REQUEST_MODE_QUERY = "q"
    private const val REQUEST_MODE_UPDATE = "u"
    private const val RESPONSE_MODE_UPDATE_SUCCESS = "cache updated"
    private const val RESPONSE_MODE_QUERY_MISS = "cache miss"
    private const val RESPONSE_MODE_INVALID_KEY = "Invalid key"
    private val REQUEST_STRING_SPLITTER = "\n".toRegex()
  }
}
