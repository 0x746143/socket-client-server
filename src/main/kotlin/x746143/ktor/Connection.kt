package x746143.ktor

import io.ktor.utils.io.*

class Connection(
    val input: ByteReadChannel,
    val output: ByteWriteChannel,
    private val client: KtorSocketClient
) : SuspendCloseable {
    override suspend fun close() {
        client.releaseConnection(this)
    }
}