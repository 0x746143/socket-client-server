package x746143.netty

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.HttpResponse

inline fun HttpResponse.addHeaders(
    contentLength: Int = 0,
    block: HttpHeaders.() -> Unit = {}
): HttpResponse {
    headers()
        .set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
        .set(HttpHeaderNames.CONTENT_LENGTH, contentLength)
        .block()
    return this
}

fun String.toBuffer(): ByteBuf {
    return Unpooled.wrappedBuffer(this.toByteArray())
}