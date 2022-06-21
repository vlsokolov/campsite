package com.example.campsite

import com.example.campsite.dto.Availabilty
import com.example.campsite.dto.ReservationDto
import com.example.campsite.dto.toEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class ReservationService(
        private val repository: ReservationRepository
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveReservation(reservation: ReservationDto, reservationId: Int? = null): Reservation {
        val existingReservationDates = repository.findAllInTimeRangeExcludeUser(
                reservation.fromDate, reservation.toDate, reservation.email
        )

        if (existingReservationDates.isNotEmpty()) throw ReservationException(
                "Campsite already reserved for provided dates. Please try another dates"
        )

        return repository.save(reservation.toEntity(reservationId))
    }

    fun getReservation(reservationId: Int): Reservation? {
        return repository.findByIdOrNull(reservationId)
    }

    @Transactional
    fun getAvailability(rangeFromDate: Instant, rangeToDate: Instant): List<Availabilty> {
        val existingReservationDates = repository.findAllInTimeRange(rangeFromDate, rangeToDate)
                .map { Availabilty(it.fromDate, it.toDate) }.sortedBy { it.fromDate }

        val availabilityRanges = mutableListOf<Availabilty>()

        if (existingReservationDates.isEmpty()) {
            availabilityRanges.add(Availabilty(rangeFromDate, rangeToDate))
            return availabilityRanges
        }

        existingReservationDates.forEach { reservation ->
            availabilityRanges.lastOrNull()
                    ?.let {
                        if (it.toDate.plus(1, ChronoUnit.DAYS) < reservation.fromDate) {
                            availabilityRanges.add(Availabilty(it.toDate.plus(1, ChronoUnit.DAYS),
                                    reservation.fromDate.minus(1, ChronoUnit.DAYS)))
                        }
                        if (it.fromDate.minus(1, ChronoUnit.DAYS) > reservation.toDate) {
                            availabilityRanges.add(Availabilty(reservation.toDate.plus(1, ChronoUnit.DAYS),
                                    it.fromDate.minus(1, ChronoUnit.DAYS)))
                        }
                    }
                    ?: availabilityRanges.add(Availabilty(rangeFromDate, reservation.fromDate.minus(1, ChronoUnit.DAYS)))
        }

        val lastRangeFrom = if (existingReservationDates.last().toDate > availabilityRanges.last().toDate) {
            existingReservationDates.last().toDate.plus(1, ChronoUnit.DAYS)
        } else {
            availabilityRanges.last().toDate.plus(1, ChronoUnit.DAYS)
        }

        if (lastRangeFrom < rangeToDate) {
            availabilityRanges.add(Availabilty(lastRangeFrom, rangeToDate))
        }

        return availabilityRanges
    }

    fun deleteReservation(reservationId: Int) {
        repository.deleteById(reservationId)
    }

    fun isExists(reservationId: Int): Boolean {
        return repository.existsById(reservationId)
    }
}