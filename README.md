# campsite

#Application written with assumption that user pass authentication 

## To streamline the reservations a few constraints need to be in place - 
###  The campsite will be free for all. 
###  The campsite can be reserved for max 3 days. 
###  The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance. 
###  Reservations can be cancelled anytime. 
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
            email = "test@example.com",
            fromDate = now,
            toDate = now.plus(2, ChronoUnit.DAYS)
        )
        
        `when`(repository.findAllInTimeRangeExcludeUser(reservation.fromDate, reservation.toDate, reservation.email))
            .thenReturn(listOf(Reservation()))

        // When/Then
        assertThrows<ReservationException> {
            service.saveReservation(reservation)
        }
    }

    @Test
    fun `test getReservation when reservation exists`() {
        // Given
        val reservationId = 1
        val expectedReservation = Reservation()
        `when`(repository.findByIdOrNull(reservationId)).thenReturn(expectedReservation)

        // When
        val result = service.getReservation(reservationId)

        // Then
        assertEquals(expectedReservation, result)
    }

    @Test
    fun `test getReservation when reservation does not exist`() {
        // Given
        val reservationId = 1
        `when`(repository.findByIdOrNull(reservationId)).thenReturn(null)

        // When
        val result = service.getReservation(reservationId)

        // Then
        assertEquals(null, result)
    }

    @Test
    fun `test getAvailability with no existing reservations`() {
        // Given
        val fromDate = Instant.now()
        val toDate = fromDate.plus(5, ChronoUnit.DAYS)
        
        `when`(repository.findAllInTimeRange(fromDate, toDate)).thenReturn(emptyList())

        // When
        val result = service.getAvailability(fromDate, toDate)

        // Then
        assertEquals(1, result.size)
        assertEquals(fromDate, result[0].fromDate)
        assertEquals(toDate, result[0].toDate)
    }

    @Test
    fun `test deleteReservation`() {
        // Given
        val reservationId = 1

        // When
        service.deleteReservation(reservationId)

        // Then
        verify(repository).deleteById(reservationId)
    }

    @Test
    fun `test isExists when reservation exists`() {
        // Given
        val reservationId = 1
        `when`(repository.existsById(reservationId)).thenReturn(true)

        // When
        val result = service.isExists(reservationId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `test isExists when reservation does not exist`() {
        // Given
        val reservationId = 1
        `when`(repository.existsById(reservationId)).thenReturn(false)

        // When
        val result = service.isExists(reservationId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `test getAvailability with one existing reservation`() {
        // Given
        val fromDate = Instant.now()
        val toDate = fromDate.plus(5, ChronoUnit.DAYS)
        val reservedFrom = fromDate.plus(2, ChronoUnit.DAYS)
        val reservedTo = fromDate.plus(3, ChronoUnit.DAYS)
        
        val existingReservation = Reservation().apply {
            this.fromDate = reservedFrom
            this.toDate = reservedTo
        }
        
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