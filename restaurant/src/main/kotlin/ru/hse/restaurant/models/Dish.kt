package ru.hse.restaurant.models

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "menu")
class Dish(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Int,
    private val name: String,
    private val description: String,
    private val price: BigDecimal,
    private val minutesToCook: Int,
    private val author: String
) {
    fun getPrice(): BigDecimal = price
    fun getName(): String = name
    fun getMinutes(): Int = minutesToCook

    override fun toString(): String {
        return "Dish with id $id, name = $name, description = $description, price = $price, minutes to cook = $minutesToCook, author = $author"
    }

    constructor() : this(0, "", "", BigDecimal(0), 0, "")
}
