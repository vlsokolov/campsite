package com.example.campsite.dto

import com.example.campsite.Reservation
import java.time.Instant

@ReservationValidator
data class ReservationDto(
        val id: Int? = null,
        val firstName: String,
        val lastName: String,
        val email: String,
        val fromDate: Instant,
        val toDate: Instant
)

data class CreateReservationDto(
        val id: Int
)

data class AvalabilityRange(
        val availability: List<Availabilty>
)

data class Availabilty(
        val fromDate: Instant,
        val toDate: Instant
)

fun ReservationDto.toEntity(id: Int? = null): Reservation {
    return Reservation(
            id = id,
            firstName = firstName,
            lastName = lastName,
            email = email,
            fromDate = fromDate,
            toDate = toDate
    )
}
