package com.test.stream

import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StatsRouteTest {
    @Test
    fun testStatsAreCreated() {
        withTestApplication({ main() }) {
            with(handleRequest(HttpMethod.Post, "/event") {
                addHeader(HttpHeaders.ContentType, ContentType.Text.CSV.toString())
                setBody("1607341341814,0.0442672968,1282509067\n" +
                        "1607341339814,0.0473002568,1785397644\n" +
                        "1607341331814,0.0899538547,1852154378")
            }) {
                Thread.sleep(3000)
                assertEquals(HttpStatusCode.Accepted, response.status())
            }

            with(handleRequest(HttpMethod.Get, "/stats")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("0,0.0000000000,0.0000000000,0,0", response.content)
            }
        }
    }
}