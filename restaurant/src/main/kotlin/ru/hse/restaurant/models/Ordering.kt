package ru.hse.restaurant.models

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalTime

@Entity
@Table(name = "orderings")
class Ordering(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Int,
    @ElementCollection
    private val dishID: MutableList<Int>,
    private val timeStarted: LocalTime,
    private val timeEnded: LocalTime,
    private val price: BigDecimal,
    private val customer: String
) {
    fun getPrice(): BigDecimal = price
    fun getCustomer(): String = customer
    constructor() : this(0, mutableListOf(), LocalTime.now(), LocalTime.now(), BigDecimal(0), "")
}
