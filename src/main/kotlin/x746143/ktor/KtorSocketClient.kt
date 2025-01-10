/*
 * Copyright 2025 0x746143
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package x746143.ktor

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout

class KtorSocketClient(private val hostname: String,
                       private val port: Int,
                       private val maxConnections: Int,
                       private val timeoutMillis: Long) {
    private val selectorManager = ActorSelectorManager(Dispatchers.IO)
    private val connectionPool: Channel<Connection> = Channel(maxConnections)
    private val socketBuilder = aSocket(selectorManager).tcp()

    suspend fun connect() {
        repeat(maxConnections) {
            val socket = socketBuilder.connect(hostname, port)
            val connection = Connection(socket.openReadChannel(), socket.openWriteChannel(true), this)
            connectionPool.send(connection)
        }
    }

    suspend fun acquireConnection(): Connection {
        return withTimeout(timeoutMillis) {
            connectionPool.receive()
        }
    }

    suspend fun releaseConnection(connection: Connection) {
        connectionPool.send(connection)
    }

    suspend fun exchange(msg: String): String? {
        acquireConnection().use { conn ->
            conn.output.writeFully(msg.toByteArray())
            return conn.input.readUTF8Line()
        }
    }
}

