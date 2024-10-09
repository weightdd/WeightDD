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

import com.google.common.hash.HashCode
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.net.Socket
import java.nio.file.Paths

@RunWith(JUnit4::class)
class CacheServerTest {
  private var socket: Socket? = null
  private val cachePath = Paths.get("global_cache/test/org/perses/globalcache/sample_cache.csv")
  private val cacheServer = CacheServer(cachePath)

  @Before
  fun setup() {
    cacheServer.start()
    socket = Socket("127.0.0.1", cacheServer.port)
  }

  @After
  fun clean() {
    cacheServer.close()
    socket!!.close()
  }

  @Test
  @Throws(Exception::class)
  fun testPing() {
    val message = "p\n"
    val response = sendAndReceive(message, socket)
    assertThat(response).isEqualTo("Pong")
  }

  @Test
  fun testQuery() {
    assertThat(cacheServer.savedTimeInMillies).isEqualTo(0)
    val message = (
      "q db883b2d3f815c5026d6b05ae650f9d303645b1e0cc487328f8f09ff60b72f54" +
        "e77fb3cc341b453e1059466c900504d9c361352b5af38412ec7d9467de506889\n"
      )
    val response = sendAndReceive(message, socket)
    assertThat(response).isEqualTo("1")
    assertThat(cacheServer.savedTimeInMillies).isEqualTo(10)
  }

  @Test
  fun testUpdate() {
    val message = """
        u db883b2d3f815c5026d6b05ae650f9d303645b1e0cc487328f8f09ff60b72f54e77fb3cc341b453e1059466c900504d9c361352b5af38412ec7d9467de506881
        1
        12
        
    """.trimIndent()
    val response = sendAndReceive(message, socket)
    assertThat(response).isEqualTo("cache updated")
    val updateDatabase = cacheServer.database
    assertThat(updateDatabase.size).isEqualTo(2)
    val key = listOf(
      (
        "db883b2d3f815c5026d6b05ae650f9d303645b1e0cc487328f8f09ff60b72f54" +
          "e77fb3cc341b453e1059466c900504d9c361352b5af38412ec7d9467de506889"
        ),
      (
        "db883b2d3f815c5026d6b05ae650f9d303645b1e0cc487328f8f09ff60b72f54" +
          "e77fb3cc341b453e1059466c900504d9c361352b5af38412ec7d9467de506881"
        ),
    ).map { HashCode.fromString(it) }
    assertThat(updateDatabase.keys).isEqualTo(HashSet(key))
    assertThat(updateDatabase[key[1]]!!.exitCode.intValue).isEqualTo(1)
    assertThat(updateDatabase[key[1]]!!.ellapsedMillies).isEqualTo(12)
  }

  private fun sendAndReceive(msg: String, socket: Socket?): String {
    val messageBytes = msg.toByteArray()
    val outputStream = socket!!.getOutputStream()
    outputStream.write(messageBytes)
    outputStream.flush()
    val inputStream = socket.getInputStream()
    val buffer = ByteArray(1024)
    val bytesRead = inputStream.read(buffer)
    var response = ""
    if (bytesRead != -1) {
      response = String(buffer, 0, bytesRead)
    }
    return response
  }
}
