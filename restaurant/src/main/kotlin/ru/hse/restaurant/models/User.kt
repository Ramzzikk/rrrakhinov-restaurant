package ru.hse.restaurant.models

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "users")
class User (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID,
    val username: String,
    val password: String,
    val type: String
) {
    constructor() : this(UUID.randomUUID(), "", "", "")
}