package ru.hse.restaurant.repositories

import org.springframework.data.jpa.repository.JpaRepository
import ru.hse.restaurant.models.Feedback

interface FeedbackRepository : JpaRepository<Feedback, Int>