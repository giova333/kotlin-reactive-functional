package com.gladunalexander.kotlinwebfluxreactive

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import java.util.*

@SpringBootConfiguration
@EnableAutoConfiguration
class KotlinWebfluxReactiveApplication {

    @Autowired
    fun register(ctx: GenericApplicationContext) = beans().initialize(ctx)

    fun beans() = beans {
        bean<PersonHandler>()
        bean {
            PersonRoutes(ref()).routes()
        }
        bean {
            CommandLineRunner {
                val repository = ref<PersonRepository>()
                repository
                        .deleteAll()
                        .thenMany(repository.saveAll(
                                Arrays.asList(
                                        Person("Bob"),
                                        Person("Dean"),
                                        Person("Sam"))))
                        .blockLast()
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<KotlinWebfluxReactiveApplication>(*args)
}

data class Person(
        val name: String,
        val id: String = UUID.randomUUID().toString()
)

interface PersonRepository : ReactiveMongoRepository<Person, String>

class PersonHandler(private val repository: PersonRepository) {

    fun getAll(request: ServerRequest) =
            ServerResponse.ok().body(repository.findAll(), Person::class.java)

    fun save(request: ServerRequest) =
            ServerResponse.ok().body(
                    request.bodyToMono(Person::class.java)
                            .flatMap { repository.save(it) },
                    Person::class.java
            )
}

class PersonRoutes(private val handler: PersonHandler) {
    fun routes() = router {
        "/persons".nest {
            GET("/", handler::getAll)
            POST("/", handler::save)
        }
    }
}
