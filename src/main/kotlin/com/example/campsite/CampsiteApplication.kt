package com.example.campsite

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories


@SpringBootApplication
@ComponentScan
@EnableJpaRepositories(basePackages = ["com.example.campsite"])
class CampsiteApplication

fun main(args: Array<String>) {
	runApplication<CampsiteApplication>(*args)
}

@Bean
fun objectMapper(): ObjectMapper {
	val objectMapper = ObjectMapper()
	objectMapper.registerModule(KotlinModule())
	objectMapper.registerModule(JavaTimeModule())
	return objectMapper
}
