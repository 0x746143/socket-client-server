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

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import x746143.nio.NioSocketServer

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