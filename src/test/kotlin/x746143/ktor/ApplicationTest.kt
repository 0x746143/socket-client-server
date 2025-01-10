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

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import x746143.nio.NioSocketServer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    private val socketServer = NioSocketServer()

    @BeforeTest
    fun setUp() {
        socketServer.start()
    }

    @AfterTest
    fun tearDown() {
        socketServer.stop()
    }

    @Test
    fun testSingleRequest() = testApp {
        client.get("?name=qwerty").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello, qwerty!", body())
        }
    }

    @Test
    fun testMultipleRequests() = testApp {
        coroutineScope {
            repeat(1000) {
                launch {
                    client.get("?name=qwerty").apply {
                        assertEquals(HttpStatusCode.OK, status)
                        assertEquals("Hello, qwerty!", body())
                    }
                }
            }
        }
    }

    @Test
    fun testLongUrl() = testApp {
        val longName = "a".repeat(1000)
        client.get("?name=$longName").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello, $longName!", body())
        }
    }

    private fun testApp(block: suspend ApplicationTestBuilder.() -> Unit) {
        testApplication {
            application {
                module(socketServer.port)
            }
            block()
        }
    }
}
