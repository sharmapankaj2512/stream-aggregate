package com.test.stream.infrastructure

import com.test.stream.main
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EventsRoutesTest {
    @Test
    fun shouldReturnBadRequestWhenInformationIsMissing() {
        withTestApplication({ main() }) {
            with(handleRequest(HttpMethod.Post, "/event") {
                addHeader(HttpHeaders.ContentType, ContentType.Text.CSV.toString())
                setBody("1607341341814,0.0442672968,")
            }) {
                Assertions.assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun shouldReturnBadRequestWhenCsvIsInValid() {
        withTestApplication({ main() }) {
            with(handleRequest(HttpMethod.Post, "/event") {
                addHeader(HttpHeaders.ContentType, ContentType.Text.CSV.toString())
                setBody("1607341341814")
            }) {
                Assertions.assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

}