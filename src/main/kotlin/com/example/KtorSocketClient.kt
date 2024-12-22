package com.example

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

