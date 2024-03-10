package ru.hse.restaurant.controllers

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import ru.hse.restaurant.models.Dish
import ru.hse.restaurant.models.User
import ru.hse.restaurant.repositories.UserRepository
import java.util.*


interface UserController {
    @PostMapping("/signUp")
    fun createUser(
        @RequestParam username: String, @RequestParam password: String, @RequestParam type: String
    ): String

    @GetMapping("/signIn")
    fun logIn(
        @RequestParam username: String, @RequestParam password: String, @RequestParam type: String
    ): String

    @DeleteMapping("/logOut")
    fun logOut(
        @RequestParam username: String, @RequestParam password: String, @RequestParam type: String
    ): String

    @GetMapping("/getDishes")
    fun getDishes(): Any

    @GetMapping("/getCurrentUser")
    fun getCurrentUser(): Any

}

@RequestMapping("/api/user")
@RestController
class UserControllerImpl(
    @Autowired private val userRepository: UserRepository,
    @Autowired private val adminController: AdminControllerImpl,
    @Autowired private val visitorController: VisitorControllerImpl
) : UserController {

    @PersistenceContext
    private lateinit var entityManager: EntityManager
    private var currUser: User? = null

    @PostMapping("/signUp")
    override fun createUser(
        @RequestParam username: String,
        @RequestParam password: String,
        @RequestParam type: String
    ): String {
        if (type != "admin" && type != "visitor") {
            return "Неверный тип пользователя (доступны только 'admin' и 'visitor')"
        }
        val query = entityManager.createNativeQuery(
            "SELECT * FROM users WHERE username = ?1 AND type = ?2", User::class.java
        ).setParameter(1, username)
            .setParameter(2, type)

        if (!query.resultList.isEmpty()) {
            return "Пользователь с таким именем и типом уже существует"
        }
        val newUser = User(UUID.randomUUID(), username, password, type)
        userRepository.save(newUser)
        return "Вы успешно зарегистрировались"
    }

    @GetMapping("/signIn")
    override fun logIn(
        @RequestParam username: String,
        @RequestParam password: String,
        @RequestParam type: String
    ): String {
        if (type != "admin" && type != "visitor") {
            return "Неверный тип пользователя (доступны только 'admin' и 'visitor')"
        }
        if (currUser != null && currUser!!.username == username) {
            return "Вы уже вошли в систему"
        }
        val query = entityManager.createNativeQuery(
            "SELECT * FROM users WHERE username = ?1 AND password = ?2 AND type = ?3", User::class.java
        ).setParameter(1, username)
            .setParameter(2, password)
            .setParameter(3, type)

        val resultList = query.resultList
        if (resultList.isEmpty()) {
            return "Пользователь не найден"
        }
        currUser = resultList.first() as User
        if (currUser!!.type == "visitor") {
            visitorController.currVisitor = currUser
            adminController.currAdmin = null
        } else {
            adminController.currAdmin = currUser
            visitorController.currVisitor = null
        }
        return "Вы успешно вошли в систему"
    }

    @DeleteMapping("/logOut")
    override fun logOut(
        @RequestParam username: String,
        @RequestParam password: String,
        @RequestParam type: String
    ): String {
        if (type != "admin" && type != "visitor") {
            return "Неверный тип пользователя (доступны только 'admin' и 'visitor')"
        }
        if (currUser == null || currUser!!.username != username) {
            return "Вы не в системе"
        }
        if (currUser!!.type == "visitor") {
            visitorController.currVisitor = null
        } else {
            adminController.currAdmin = null
        }
        currUser = null
        return "Вы успешно вышли из системы"
    }

    @GetMapping("/getDishes")
    override fun getDishes(): Any {
        val query = entityManager.createNativeQuery("SELECT * FROM menu", Dish::class.java)
        return query.resultList
    }

    @GetMapping("/getCurrentUser")
    override fun getCurrentUser(): Any {
        return currUser ?: "Нет пользователя в системе"
    }

}