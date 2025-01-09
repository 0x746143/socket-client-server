package x746143.netty

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import org.slf4j.LoggerFactory

class HttpServerHandler : SimpleChannelInboundHandler<FullHttpRequest>() {

    companion object {
        private val logger = LoggerFactory.getLogger(HttpServerHandler::class.java)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, request: FullHttpRequest) {
        val response = when (request.uri()) {
            "/" -> redirect("/hello")
            "/hello" -> responseString("Hello, world")
            else -> notFound()
        }
        ctx.writeAndFlush(response)
    }

    @Suppress("SameParameterValue")
    private fun responseString(content: String): HttpResponse {
        return httpResponse(content.toBuffer()).addHeaders(content.length)
    }

    @Suppress("SameParameterValue")
    private fun redirect(url: String): HttpResponse {
        return httpResponse(HttpResponseStatus.FOUND).addHeaders {
            set(HttpHeaderNames.LOCATION, url)
        }
    }

    private fun notFound(): HttpResponse {
        return httpResponse(HttpResponseStatus.NOT_FOUND).addHeaders()
    }

    private fun httpResponse(status: HttpResponseStatus = HttpResponseStatus.OK): HttpResponse {
        return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status)
    }

    private fun httpResponse(content: ByteBuf = Unpooled.buffer(0)): HttpResponse {
        return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.error(cause.message)
        ctx.close()
    }
}