package com.example.campsite.dto

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.regex.Pattern
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ReservationConstraintValidator::class])
annotation class ReservationValidator(
    val message: String = "Invalid input parameter",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

const val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$"

class ReservationConstraintValidator : ConstraintValidator<ReservationValidator, ReservationDto> {
    override fun isValid(value: ReservationDto?, context: ConstraintValidatorContext?): Boolean {
        var isValid = true
        return value?.let {
                when {
                    it.firstName.isBlank() -> {
                        context?.buildConstraintViolationWithTemplate(
                                "First name shouldn't be blank"
                        )?.addPropertyNode("firstName")?.addConstraintViolation()
                        isValid = false
                    }
                    it.lastName.isBlank() -> {
                        context?.buildConstraintViolationWithTemplate(
                                "Last name shouldn't be blank"
                        )?.addPropertyNode("lastName")?.addConstraintViolation()
                        isValid = false
                    }
                    !validateEmail(it.email) -> {
                        context?.buildConstraintViolationWithTemplate(
                                "Provided email not valid"
                        )?.addPropertyNode("email")?.addConstraintViolation()
                        isValid = false
                    }
                    Instant.now().plus(1, ChronoUnit.DAYS) > it.fromDate -> {
                        context?.buildConstraintViolationWithTemplate(
                                "Unable to book campsite less than 1 day to arrival"
                        )?.addPropertyNode("fromDate")?.addConstraintViolation()
                        isValid = false
                    }
                    it.fromDate.isAfter(it.toDate) -> {
                        context?.buildConstraintViolationWithTemplate(
                                "Reservation start date is after end date"
                        )?.addPropertyNode("fromDate")?.addConstraintViolation()
                        isValid = false
                    }
                    it.toDate.minus(3, ChronoUnit.DAYS) > it.fromDate -> {
                        context?.buildConstraintViolationWithTemplate(
                                "Max reservation are 3 days"
                        )?.addPropertyNode("toDate")?.addConstraintViolation()
                        isValid = false
                    }
                }
                isValid
        } ?: false
    }
}

fun validateEmail(email: String): Boolean {
    return Pattern.compile(EMAIL_REGEX).matcher(email).matches()
}