package com.gladunalexander.example

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.SerializationFeature
import com.github.salomonbrys.kodein.*
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.content.resources
import io.ktor.content.static
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.jackson.jackson
import io.ktor.locations.*
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.litote.kmongo.KMongo
import org.litote.kmongo.save
import java.util.*

/**
 * Created by Alexander Gladun on 2019-05-05.
 */

data class User(
        val name: String,
        val id: String = UUID.randomUUID().toString()
)

@Location("/list/{name}/page/{page}")
data class Listing(val name: String, val page: Int, val queryParam: String)

class UserRepository(private val client: MongoClient) {

    private val collection: MongoCollection<User> = client.getDatabase("db").getCollection("users", User::class.java)

    fun findAll() = collection.find().toList()

    fun save(user: User): User {
        collection.save(user)
        return user
    }
}

fun Application.main() {
    install(DefaultHeaders)
    install(Compression)
    install(CallLogging)
    install(Locations)
    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
            setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
                indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
                indentObjectsWith(DefaultIndenter("  ", "\n"))
            })
        }
    }

    val kodein = Kodein {
        bind<MongoClient>() with singleton { KMongo.createClient() }
        bind<UserRepository>() with singleton { UserRepository(instance()) }
    }

    initRoutes(kodein)
}

fun Application.initRoutes(kodein: Kodein) {
    val userRepository: UserRepository = kodein.instance()

    routing {
        get("/users") {
            call.respond(userRepository.findAll())
        }
        post("/users") {
            val userRequest = call.receive(User::class)
            call.respond(userRepository.save(userRequest))
        }

        get<Listing> { listing ->
            call.respond(listing)
        }
        static("static") {
            resources("static")
        }
    }

}

fun main() {
    embeddedServer(Netty, port = 8080) {
        main()
    }.start(wait = true)
}