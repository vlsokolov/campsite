package com.example.campsite

import com.example.campsite.dto.AvalabilityRange
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.temporal.ChronoUnit

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationControllerTest {

    private val fromDate = Instant.now().plus(10, ChronoUnit.DAYS)
    private val toDate = fromDate.plus(3, ChronoUnit.DAYS)

    private val reservation = Reservation(
            id = 1,
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@gmail.com",
            fromDate = fromDate,
            toDate = toDate
    )

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var repository: ReservationRepository

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun saveReservation() {
        mockMvc.post("/reservation") {
            content = objectMapper().writeValueAsString(reservation)
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun tryToSaveOverlappedDates() {
        mockMvc.post("/reservation") {
            content = objectMapper().writeValueAsString(reservation)
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }

        mockMvc.post("/reservation") {
            content = objectMapper().writeValueAsString(reservation.copy(email = "testemail@gmail.com"))
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { string("Campsite already reserved for provided dates. Please try another dates") }
        }
    }

    @Test
    fun requestValidation() {
        run {
            @Language("JSON")
            val postResponseJson = """
            {
              "firstName": "",
              "lastName" : "Doe",
              "email" : "john.doe@gmail.com",
              "fromDate" : "$fromDate",
              "toDate" : "$toDate"
            }
            """.trimIndent()

            @Language("JSON")
            val errorMessage = """
                {
                    "First name shouldn't be blank":""
                }
            """.trimIndent()

            mockMvc.post("/reservation") {
                content = postResponseJson
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json(errorMessage) }
            }
        }

        run {
            @Language("JSON")
            val postResponseJson = """
            {
              "firstName": "John",
              "lastName" : "",
              "email" : "john.doe@gmail.com",
              "fromDate" : "$fromDate",
              "toDate" : "$toDate"
            }
            """.trimIndent()

            @Language("JSON")
            val errorMessage = """
                {
                    "Last name shouldn't be blank":""
                }
            """.trimIndent()

            mockMvc.post("/reservation") {
                content = postResponseJson
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json(errorMessage) }
            }
        }

        run {
            @Language("JSON")
            val postResponseJson = """
            {
              "firstName": "John",
              "lastName" : "Doe",
              "email" : "23#&*@gmail.com",
              "fromDate" : "$fromDate",
              "toDate" : "$toDate"
            }
            """.trimIndent()

            @Language("JSON")
            val errorMessage = """
                {
                    "Provided email not valid":"23#&*@gmail.com"
                }
            """.trimIndent()

            mockMvc.post("/reservation") {
                content = postResponseJson
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json(errorMessage) }
            }
        }

        run {

            val newReservationDate = Instant.now()
            @Language("JSON")
            val postResponseJson = """
            {
              "firstName": "John",
              "lastName" : "Doe",
              "email" : "john.doe@gmail.com",
              "fromDate" : "$newReservationDate",
              "toDate" : "$toDate"
            }
            """.trimIndent()

            @Language("JSON")
            val errorMessage = """
                {
                    "Unable to book campsite less than 1 day to arrival": "$newReservationDate"
                }
            """.trimIndent()

            mockMvc.post("/reservation") {
                content = postResponseJson
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json(errorMessage) }
            }
        }

        run {
            @Language("JSON")
            val postResponseJson = """
            {
              "firstName": "John",
              "lastName" : "Doe",
              "email" : "john.doe@gmail.com",
              "fromDate" : "$toDate",
              "toDate" : "$fromDate"
            }
            """.trimIndent()

            @Language("JSON")
            val errorMessage = """
                {
                    "Reservation start date is after end date" : "$toDate"
                }
            """.trimIndent()

            mockMvc.post("/reservation") {
                content = postResponseJson
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json(errorMessage) }
            }
        }

        run {
            @Language("JSON")
            val postResponseJson = """
            {
              "firstName": "John",
              "lastName" : "Doe",
              "email" : "john.doe@gmail.com",
              "fromDate" : "$fromDate",
              "toDate" : "${toDate.plus(2, ChronoUnit.DAYS)}"
            }
            """.trimIndent()

            @Language("JSON")
            val errorMessage = """
                {
                    "Max reservation are 3 days": "${toDate.plus(2, ChronoUnit.DAYS)}"
                }
            """.trimIndent()

            mockMvc.post("/reservation") {
                content = postResponseJson
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json(errorMessage) }
            }
        }
    }

    @Test
    fun getReservation() {
        val postResponse = mockMvc.post("/reservation") {
            content = objectMapper().writeValueAsString(reservation)
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }.andReturn()

        val reservationId = getResponseJson(postResponse)?.get("id")

        @Language("JSON")
        val postResponseJson = """
        {
          "id": $reservationId,
          "firstName" : "John",
          "lastName" : "Doe",
          "email" : "john.doe@gmail.com",
          "fromDate" : "$fromDate",
          "toDate" : "$toDate"
        }
        """.trimIndent()

        mockMvc.get("/reservation/$reservationId")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    content { json(postResponseJson) }
                }
    }

    @Test
    fun getAvailability() {
        val response = mockMvc.get("/reservation/availability")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                }.andReturn()

        val ranges = objectMapper().readValue(response.response.contentAsString, AvalabilityRange::class.java)

        assertNotNull(ranges)
        assertNotNull(ranges.availability)
        assertEquals(1, ranges.availability.size)

        mockMvc.post("/reservation") {
            content = objectMapper().writeValueAsString(reservation)
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }.andReturn()

        val newResponse = mockMvc.get("/reservation/availability")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                }.andReturn()

        val newRanges = objectMapper().readValue(newResponse.response.contentAsString, AvalabilityRange::class.java)

        assertNotNull(newRanges)
        assertNotNull(newRanges.availability)
        assertEquals(2, newRanges.availability.size)
    }

    @Test
    fun deleteReservation() {
        // if reservation is absent it's not an error
        mockMvc.delete("/reservation/1")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                }

        val postResponse = mockMvc.post("/reservation") {
            content = objectMapper().writeValueAsString(reservation)
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }.andReturn()

        val reservationId = getResponseJson(postResponse)?.get("id")

        //delete existing reservation
        mockMvc.delete("/reservation/$reservationId")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                }
    }

    @Test
    fun updateReservation() {
        mockMvc.put("/reservation/1") {
            content = objectMapper().writeValueAsString(reservation)
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
        }

        val postResponse = mockMvc.post("/reservation") {
            content = objectMapper().writeValueAsString(reservation)
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }.andReturn()

        val reservationId = getResponseJson(postResponse)?.get("id")

        //delete existing reservation
        mockMvc.put("/reservation/$reservationId") {
            content = objectMapper().writeValueAsString(reservation)
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Throws(Exception::class)
    private fun getResponseJson(result: MvcResult): JsonNode? {
        val mapper = ObjectMapper()
        return mapper.readTree(result.response.getContentAsString(StandardCharsets.UTF_8))
    }
}