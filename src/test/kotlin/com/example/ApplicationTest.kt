package com.example

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
