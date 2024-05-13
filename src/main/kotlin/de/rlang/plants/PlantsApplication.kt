package de.rlang.plants

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PlantsApplication

fun main(args: Array<String>) {
    runApplication<PlantsApplication>(*args)
}
