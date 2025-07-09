package com.example.campsite

import com.example.campsite.dto.ReservationDto
import com.example.campsite.dto.toEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

class ReservationServiceTest {

    @Mock
    private lateinit var repository: ReservationRepository

    private lateinit var service: ReservationService

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        service = ReservationService(repository)
    }

    @Test
    fun `test saveReservation when dates are available`() {
        // Given
        val now = Instant.now()
        val reservation = ReservationDto(
            firstName = "John",
            lastName = "Doe",
            email = "test@example.com",
            fromDate = now,
            toDate = now.plus(2, ChronoUnit.DAYS)
        )
        val expectedReservation = reservation.toEntity(null)

        `when`(repository.findAllInTimeRangeExcludeUser(reservation.fromDate, reservation.toDate, reservation.email))
            .thenReturn(emptyList())
        `when`(repository.save(any())).thenReturn(expectedReservation)

        // When
        val result = service.saveReservation(reservation)

        // Then
        assertEquals(expectedReservation, result)
        verify(repository).findAllInTimeRangeExcludeUser(reservation.fromDate, reservation.toDate, reservation.email)
        verify(repository).save(any())
    }

    @Test
    fun `test saveReservation when dates are not available`() {
        // Given
        val now = Instant.now()
        val reservation = ReservationDto(
            firstName = "John",
            lastName = "Doe",
            email = "test@example.com",
            fromDate = now,
            toDate = now.plus(2, ChronoUnit.DAYS)
        )

        val existingReservation = Reservation(
            firstName = "Jane",
            lastName = "Smith",
            email = "jane@example.com",
            fromDate = now,
            toDate = now.plus(2, ChronoUnit.DAYS)
        )

        `when`(repository.findAllInTimeRangeExcludeUser(reservation.fromDate, reservation.toDate, reservation.email))
            .thenReturn(listOf(existingReservation))

        // When/Then
        assertThrows<ReservationException> {
            service.saveReservation(reservation)
        }
    }

    @Test
    fun `test getReservation when reservation exists`() {
        // Given
        val reservationId = 1
        val expectedReservation = Reservation(
            firstName = "John",
            lastName = "Doe",
            email = "test@example.com",
            fromDate = Instant.now(),
            toDate = Instant.now().plus(2, ChronoUnit.DAYS)
        )
        `when`(repository.findByIdOrNull(reservationId)).thenReturn(expectedReservation)

        // When
        val result = service.getReservation(reservationId)

        // Then
        assertEquals(expectedReservation, result)
    }

    @Test
    fun `test getAvailability with one existing reservation`() {
        // Given
        val fromDate = Instant.now()
        val toDate = fromDate.plus(5, ChronoUnit.DAYS)
        val reservedFrom = fromDate.plus(2, ChronoUnit.DAYS)
        val reservedTo = fromDate.plus(3, ChronoUnit.DAYS)

        val existingReservation = Reservation(
            firstName = "John",
            lastName = "Doe",
            email = "test@example.com",
            fromDate = reservedFrom,
            toDate = reservedTo
        )

        `when`(repository.findAllInTimeRange(fromDate, toDate))
            .thenReturn(listOf(existingReservation))

        // When
        val result = service.getAvailability(fromDate, toDate)

        // Then
        assertEquals(2, result.size)
        assertEquals(fromDate, result[0].fromDate)
        assertEquals(reservedFrom.minus(1, ChronoUnit.DAYS), result[0].toDate)
        assertEquals(reservedTo.plus(1, ChronoUnit.DAYS), result[1].fromDate)
        assertEquals(toDate, result[1].toDate)
    }
}