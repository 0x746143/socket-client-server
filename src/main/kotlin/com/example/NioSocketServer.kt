package com.example

import io.ktor.util.network.*
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class NioSocketServer(var port: Int = 0) {
    companion object {
        private val logger = LoggerFactory.getLogger(NioSocketServer::class.java)
        private val beginMessage = "Hello, ".toByteArray()
        private val endMessage = "!\n".toByteArray()
    }

    private val buffer = ByteBuffer.allocateDirect(1024)
    private var executor = Executors.newSingleThreadExecutor()

    init {
        buffer.put(beginMessage)
    }

    fun start() {
        if (executor.isShutdown) {
            executor = Executors.newSingleThreadExecutor()
        }

        val selector = Selector.open()
        val serverChannel = ServerSocketChannel.open()
        serverChannel
            .bind(InetSocketAddress(port))
            .configureBlocking(false)
            .register(selector, SelectionKey.OP_ACCEPT)
        if (port == 0) {
            port = serverChannel.localAddress?.port ?: 0
        }
        logger.info("Socket server started on port $port")

        executor.execute {
            serverChannel.use {
                selector.use {
                    startSelector(selector)
                }
            }
        }
    }

    private fun startSelector(selector: Selector) {
        while (!Thread.currentThread().isInterrupted) {
            if (selector.select(1000) == 0) {
                continue
            }
            val iterator = selector.selectedKeys().iterator()
            while (iterator.hasNext()) {
                val key = iterator.next()
                iterator.remove()
                handleKey(key)
            }
        }
    }

    private fun handleKey(key: SelectionKey) {
        try {
            when {
                key.isAcceptable -> (key.channel() as ServerSocketChannel).accept()
                    .configureBlocking(false)
                    .register(key.selector(), SelectionKey.OP_READ)

                key.isReadable -> handleRead(key)
            }
        } catch (e: IOException) {
            logger.error("Socket server error: " + e.message)
            key.cancel()
            key.channel().close()
        }
    }

    private fun handleRead(key: SelectionKey) {
        val clientChannel = key.channel() as SocketChannel
        buffer.position(beginMessage.size)
        val bytes = clientChannel.read(buffer)
        if (bytes == -1) {
            logger.warn("Connection closed by client")
            key.cancel()
            clientChannel.close()
            return
        }
        buffer.put(endMessage).flip()
        clientChannel.write(buffer)
    }

    fun stop() {
        executor.shutdownNow()
        executor.awaitTermination(1, TimeUnit.SECONDS)
        logger.info("Socket server stopped")
    }
}