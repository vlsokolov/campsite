package com.example.campsite

import com.example.campsite.dto.AvalabilityRange
import com.example.campsite.dto.CreateReservationDto
import com.example.campsite.dto.ReservationDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.validation.Valid


@RestController
@RequestMapping("/reservation")
class ReservationController(
        private val reservationService: ReservationService
) {

    @GetMapping("/{reservationId}")
    fun getReservation(@PathVariable reservationId: Int): ResponseEntity<ReservationDto> {
        return reservationService.getReservation(reservationId)
                ?.let { ResponseEntity.ok(it.toDto()) }
                ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/availability")
    fun getAvailability(): ResponseEntity<AvalabilityRange> {
        val rangeFromDate = Instant.now().plus(1, ChronoUnit.DAYS)
        val rangeToDate = rangeFromDate.plus(30, ChronoUnit.DAYS)
        return ResponseEntity.ok(AvalabilityRange(reservationService.getAvailability(rangeFromDate, rangeToDate)))
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createReservation(@Valid @RequestBody dto: ReservationDto): ResponseEntity<*> {
        return try {
            ResponseEntity.status(HttpStatus.CREATED).body(CreateReservationDto(reservationService.saveReservation(dto).id!!))
        } catch (ex: ReservationException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message)
        }
    }

    @PutMapping("/{reservationId}")
    fun updateReservation(
            @PathVariable reservationId: Int,
            @Valid @RequestBody dto: ReservationDto
    ): ResponseEntity<ReservationDto> {
        return if (reservationService.isExists(reservationId)) {
            reservationService.saveReservation(dto, reservationId)
            ResponseEntity.ok(dto)
        } else {
            ResponseEntity.notFound().build()
        }

    }

    @DeleteMapping("/{reservationId}")
    fun deleteReservation(@PathVariable reservationId: Int): ResponseEntity<Boolean> {
        reservationService.deleteReservation(reservationId)
        return ResponseEntity.ok(true)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): Map<String, Any?>? {
        val errors: MutableMap<String, Any?> = HashMap()
        ex.bindingResult.allErrors
                .filterIsInstance<FieldError>()
                .forEach {
                    val errorMessage = it.defaultMessage ?: it.field
                    val value = it.rejectedValue
                    errors[errorMessage] = value
                }
        return errors
    }
}