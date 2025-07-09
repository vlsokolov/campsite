package com.example.campsite

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.LockModeType

@Repository
interface ReservationRepository: JpaRepository<Reservation, Int> {

    @Query("SELECT r FROM Reservation r where r.fromDate >= :from AND r.toDate <= :to")
    fun findAllInTimeRange(@Param("from") from: Instant, @Param("to") to: Instant): List<Reservation>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r where r.fromDate >= :from AND r.toDate <= :to AND r.email != :email")
    fun findAllInTimeRangeExcludeUser(
            @Param("from") from: Instant,
            @Param("to") to: Instant,
            @Param("email") email: String
    ): List<Reservation>

    fun findByIdOrNull(id: Int): Reservation?
}