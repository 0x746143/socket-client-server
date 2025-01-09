package x746143.ktor

interface SuspendCloseable {
    suspend fun close()
}

suspend inline fun <T : SuspendCloseable, R> T.use(block: (T) -> R): R {
    return try {
        block(this)
    } finally {
        this.close()
    }
}
