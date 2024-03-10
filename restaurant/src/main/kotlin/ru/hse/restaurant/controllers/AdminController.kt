package ru.hse.restaurant.controllers

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import ru.hse.restaurant.models.Dish
import ru.hse.restaurant.models.Ordering
import ru.hse.restaurant.models.User
import ru.hse.restaurant.repositories.MenuRepository
import java.math.BigDecimal
import java.time.LocalTime

@RequestMapping("/api/admin")
@RestController
class AdminControllerImpl(
    @Autowired private val menuRepository: MenuRepository,
) : AdminController {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override var currAdmin: User? = null

    @PostMapping("/createDish")
    override fun createDish(
        @RequestParam name: String,
        @RequestParam description: String,
        @RequestParam price: BigDecimal,
        @RequestParam minutesToCook: Int
    ): String {
        if (currAdmin == null) {
            return "Администратор не в системе"
        }
        val dish = Dish(0, name, description, price, minutesToCook, currAdmin!!.username)
        return try {
            menuRepository.save(dish)
            "Блюдо успешно создано"
        } catch (e: Exception) {
            "Такое блюдо уже существует"
        }
    }

    @DeleteMapping("/deleteDish")
    override fun deleteDish(
        @RequestParam name: String,
        @RequestParam description: String,
        @RequestParam price: BigDecimal,
        @RequestParam minutesToCook: Int
    ): String {
        if (currAdmin == null) {
            return "Администратор не в системе"
        }
        val query = entityManager.createNativeQuery(
            "SELECT * FROM menu WHERE name = '$name' AND description = '$description' AND price = '$price' AND minutes_to_cook = '$minutesToCook'",
            Dish::class.java
        )
        if (query.resultList.isEmpty()) {
            return "Такое блюдо не существует"
        }
        val dish = query.resultList.first() as Dish
        return try {
            menuRepository.delete(dish)
            "Блюдо успешно удалено"
        } catch (e: Exception) {
            "Произошла ошибка"
        }
    }

    @GetMapping("/getRevenue")
    override fun getRevenue(): Any {
        if (currAdmin == null) {
            return "Администратор не в системе"
        }
        val query = entityManager.createNativeQuery("SELECT SUM(price) FROM orderings")
        return query.singleResult as BigDecimal
    }

    @GetMapping("/getDishesSortedByPopularity")
    override fun getDishesSortedByPopularity(): Any {
        if (currAdmin == null) {
            return "Администратор не в системе"
        }
        var query =
            entityManager.createNativeQuery("SELECT dishid FROM ordering_dishid GROUP BY dishid ORDER BY COUNT(*) DESC")
        val dishIds = query.resultList as List<*>
        val ans: MutableList<String> = mutableListOf()
        for (id in dishIds) {
            query = entityManager.createNativeQuery("SELECT * FROM menu WHERE id = '$id'", Dish::class.java)
            val dish = query.singleResult as Dish
            ans.add(dish.getName())
        }
        return ans
    }

    @GetMapping("/getAverageAssessment")
    override fun getAverageAssessment(): Any {
        if (currAdmin == null) {
            return "Администратор не в системе"
        }
        val query = entityManager.createNativeQuery("SELECT AVG(assessment) FROM feedbacks")
        return query.singleResult as BigDecimal
    }

    @GetMapping("/getAllOrdersInPeriod")
    override fun getAllOrdersInPeriod(
        @RequestParam start: LocalTime,
        @RequestParam end: LocalTime,
    ): Any {
        if (currAdmin == null) {
            return "Администратор не в системе"
        }
        val query = entityManager.createNativeQuery(
            "SELECT * FROM orderings WHERE (time_started BETWEEN '$start' AND '$end') AND (time_ended BETWEEN '$start' AND '$end')",
            Ordering::class.java
        )
        return query.resultList as List<*>
    }
}

interface AdminController {
    fun createDish(
        name: String,
        description: String,
        price: BigDecimal,
        minutesToCook: Int
    ): String

    fun deleteDish(
        name: String,
        description: String,
        price: BigDecimal,
        minutesToCook: Int
    ): String

    fun getRevenue(): Any

    fun getDishesSortedByPopularity(): Any

    fun getAverageAssessment(): Any

    fun getAllOrdersInPeriod(
        start: LocalTime,
        end: LocalTime,
    ): Any

    var currAdmin: User?
}
