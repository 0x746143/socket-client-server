package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*

private val socketServer = NioSocketServer(12345)

fun main(args: Array<String>) {
    socketServer.start()
    Runtime.getRuntime().addShutdownHook(Thread { socketServer.stop() })
    io.ktor.server.cio.EngineMain.main(args)
}

fun Application.module(port: Int = socketServer.port) {
    val client = KtorSocketClient("localhost", port, 10, 1000)
    runBlocking(Dispatchers.IO) {
        println("Connect to port $port")
        client.connect()
    }

    routing {
        get("/") {
            val name = call.request.queryParameters["name"]
            if (name == null) {
                call.respond(HttpStatusCode.NotFound, "Query parameter 'name' is undefined")
                return@get
            }

            val result = client.exchange(name)
            if (result != null) {
                call.respondText(result)
            } else {
                call.respond(HttpStatusCode.ServiceUnavailable)
            }
        }
    }
}