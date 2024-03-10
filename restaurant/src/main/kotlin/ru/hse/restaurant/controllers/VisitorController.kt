package ru.hse.restaurant.controllers

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import ru.hse.restaurant.models.Dish
import ru.hse.restaurant.models.Feedback
import ru.hse.restaurant.models.Ordering
import ru.hse.restaurant.models.User
import ru.hse.restaurant.repositories.FeedbackRepository
import ru.hse.restaurant.repositories.OrderingRepository
import ru.hse.restaurant.services.KitchenServiceImpl
import java.math.BigDecimal
import java.time.LocalTime

interface VisitorController {
    @PostMapping("/createOrder")
    suspend fun createOrder(dishId: MutableList<Int>): String
    @PostMapping("/payOrder")
    fun payOrder(
        @RequestParam orderId: Int,
        @RequestParam sum: BigDecimal
    ): String

    @DeleteMapping("/cancelOrder")
    fun cancelOrder(): String

    @PutMapping("/expandOrder")
    suspend fun expandOrder(
        @RequestParam dishId: MutableList<Int>,
    ): String

    @PostMapping("/giveFeedback")
    fun giveFeedback(
        @RequestParam orderId: Int,
        @RequestParam assessment: Int,
        @RequestParam text: String
    ): String
    var currVisitor: User?
}

@RequestMapping("/api/visitor")
@RestController
class VisitorControllerImpl(
    @Autowired private val orderingRepository: OrderingRepository,
    @Autowired private val feedbackRepository: FeedbackRepository,
    @Autowired private val kitchen: KitchenServiceImpl,
) : VisitorController {
    override var currVisitor: User? = null

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @PostMapping("/createOrder")
    override suspend fun createOrder(
        @RequestParam dishId: MutableList<Int>
    ): String {
        if (currVisitor == null) {
            return "Посетитель не в системе"
        }
        val timeStart = LocalTime.now()
        val dishes: MutableList<Dish>
        try {
            dishes = getAllDishesByIds(dishId)
        } catch (e: Exception) {
            return e.message!!
        }

        return if (kitchen.serveOrder(currVisitor!!.username, dishes)) {
            "Заказ был отменен"
        } else {
            val timeEnd = LocalTime.now()
            val price = kitchen.cookedDishes.sumOf { dish -> dish.getPrice() }
            val order = Ordering(0, dishId, timeStart, timeEnd, price, currVisitor!!.username)
            withContext(Dispatchers.IO) {
                orderingRepository.save(order)
            }
            kitchen.cookedDishes.clear()
            "Заказ был сохранен в базе данных"
        }
    }

    @PostMapping("/payOrder")
    override fun payOrder(
        @RequestParam orderId: Int,
        @RequestParam sum: BigDecimal
    ): String {
        if (currVisitor == null) {
            return "Нет визитера залогиненного"
        }
        val query = entityManager.createNativeQuery(
            "SELECT * FROM orderings WHERE id = '$orderId'",
            Ordering::class.java
        )
        val resultList = query.resultList
        if (resultList.isEmpty()) {
            return "Заказ с таким идентификатором не найден"
        }
        val order = resultList.first() as Ordering
        if (sum < order.getPrice()) {
            return "Недостаточно средств для оплаты заказа"
        }
        println("Чаевые ${sum - order.getPrice()}. Вы согласны? (Y/N)")
        var command: String = readln()
        while (command.uppercase() != "Y" && command.uppercase() != "N") {
            println("Неверное имя - попробуй еще раз")
            command = readln()
        }

        var newSum: BigDecimal = sum
        while (command.uppercase() == "N") {
            println("Напишите другую сумму")
            newSum = readln().toBigDecimal()
            while (newSum < order.getPrice()) {
                println("Вы должны оплатить, как минимум, сумму заказа, поэтому попробуйте еще раз")
                newSum = readln().toBigDecimal()
            }

            println("Tips are ${newSum - order.getPrice()}. Do you confirm? (Y/N)")
            command = readln()
            while (command.uppercase() != "Y" && command.uppercase() != "N") {
                println("Wrong letter, come again")
                command = readln()
            }
        }
        println("Revenue is $newSum")
        if (newSum - order.getPrice() == BigDecimal(777.00)) {
            println("Tips for waiters are three axes")
        } else {
            println("Tips for waiters are ${newSum - order.getPrice()}")
        }
        return "Заказ успешно оплачен"
    }

    @DeleteMapping("/cancelOrder")
    override fun cancelOrder(): String {
        if (currVisitor == null) {
            return "No logged visitor"
        }
        kitchen.cancelOrder(currVisitor!!.username)
        return "Заказ успешно отменен"
    }

    @PutMapping("/expandOrder")
    override suspend fun expandOrder(
        @RequestParam dishId: MutableList<Int>,
    ): String {
        if (currVisitor == null) {
            return "No logged visitor"
        }
        val dishes: MutableList<Dish>
        try {
            dishes = getAllDishesByIds(dishId)
        } catch (e: Exception) {
            return e.message!!
        }
        kitchen.expandOrder(currVisitor!!.username, dishes)
        return "Заказ успешно расширен"
    }

    @PostMapping("/giveFeedback")
    override fun giveFeedback(
        @RequestParam orderId: Int,
        @RequestParam assessment: Int,
        @RequestParam text: String
    ): String {
        if (currVisitor == null) {
            return "No logged visitor"
        }
        val query =
            entityManager.createNativeQuery("SELECT * FROM orderings WHERE id = '$orderId'", Ordering::class.java)
        val resultList = query.resultList
        if (resultList.isEmpty()) {
            return "Заказ с таким идентификатором не найден"
        }
        if ((resultList.first() as Ordering).getCustomer() != currVisitor!!.username) {
            return "Это не ваш заказ"
        }
        if (assessment !in 1..5) {
            return "Оценка должна быть в диапазоне от 1 до 5"
        }
        val feedback = Feedback(0, orderId, assessment, text)
        feedbackRepository.save(feedback)
        return "Отзыв был сохранен"
    }

    private fun getAllDishesByIds(dishId: MutableList<Int>): MutableList<Dish> {
        val dishes: MutableList<Dish> = mutableListOf()
        for (id in dishId) {
            val query = entityManager.createNativeQuery(
                "SELECT * FROM menu WHERE id = '$id'",
                Dish::class.java
            )
            val resultList = query.resultList
            if (resultList.isEmpty()) {
                throw Exception("Блюдо с ID $id не найдено")
            }
            val dish = resultList.first() as Dish
            dishes.add(dish)
        }
        return dishes
    }
}