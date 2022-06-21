package com.example.campsite

import com.example.campsite.dto.ReservationDto
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Table(name = "reservations")
@Entity
data class Reservation(
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    var id: Int? = null,

    @Column(name = "first_name")
    var firstName: String,

    @Column(name = "last_name")
    var lastName: String,

    var email: String,

    @Column(name = "from_date")
    var fromDate: Instant,

    @Column(name = "to_date")
    var toDate: Instant
)

fun Reservation.toDto(): ReservationDto = ReservationDto(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        fromDate = fromDate,
        toDate = toDate
)