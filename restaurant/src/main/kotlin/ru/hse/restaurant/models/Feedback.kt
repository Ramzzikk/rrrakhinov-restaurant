package ru.hse.restaurant.models

import jakarta.persistence.*

@Entity
@Table(name = "feedbacks")
class Feedback(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Int,
    private val orderId: Int,
    private val assessment: Int,
    private val text: String,
) {
    constructor() : this(0, 0, 0, "")
}