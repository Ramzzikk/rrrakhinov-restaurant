package ru.hse.restaurant.services

import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import ru.hse.restaurant.models.Dish


interface KitchenService {
    suspend fun serveOrder(username: String, dishes: MutableList<Dish>): Boolean
    suspend fun expandOrder(username: String, dishes: MutableList<Dish>)
    fun cancelOrder(username: String)
}

@Service
class KitchenServiceImpl : KitchenService {

    private lateinit var cookingJobs: List<Job>
    val cookedDishes: MutableList<Dish> = mutableListOf()
    private var isCanceled: Boolean = false

    private suspend fun cookDish(username: String, dish: Dish) = coroutineScope {
        println("Dish ${dish.getName()} for $username is cooking...")
        delay((dish.getMinutes().toLong()) * 1000) // 1 minute = 1 second
        cookedDishes.add(dish)
        println("Dish ${dish.getName()} for $username was cooked!")
    }

    override suspend fun serveOrder(username: String, dishes: MutableList<Dish>): Boolean = coroutineScope {
        println("The order for $username has started to be serviced...")
        cookingJobs = dishes.map { dish ->
            launch { cookDish(username, dish) }
        }
        cookingJobs.joinAll()
        if (isCanceled) println("The order for $username was cancelled by its requester")
        else println("The order for $username has finished to be serviced...")
        return@coroutineScope isCanceled
    }

    override fun cancelOrder(username: String) {
        for (job in cookingJobs) {
            job.cancel()
        }
        isCanceled = true
    }

    override suspend fun expandOrder(username: String, dishes: MutableList<Dish>): Unit = coroutineScope {
        println("User $username is expanding his order")
        val supplementCookingJobs = dishes.map { dish ->
            launch { cookDish(username, dish) }
        }
        cookingJobs.plus(supplementCookingJobs)
    }

}