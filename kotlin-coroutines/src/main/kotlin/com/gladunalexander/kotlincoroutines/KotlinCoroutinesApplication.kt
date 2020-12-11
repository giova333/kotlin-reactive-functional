package com.gladunalexander.kotlincoroutines

import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.annotation.Id
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import org.springframework.stereotype.Component
import org.springframework.util.MimeTypeUtils.APPLICATION_JSON
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.blockhound.BlockHound

@SpringBootApplication
class KotlinCoroutinesApplication {

    @Bean
    fun initialize(factory: ConnectionFactory) = ConnectionFactoryInitializer()
            .apply {
                setConnectionFactory(factory)
                val populator = CompositeDatabasePopulator()
                        .apply {
                            addPopulators(
                                    ResourceDatabasePopulator(ClassPathResource("/schema.sql"))
                            )
                        }
                setDatabasePopulator(populator)
            }
}

fun main(args: Array<String>) {
    BlockHound.install()
    runApplication<KotlinCoroutinesApplication>(*args)
}

data class User(
        @Id
        val id: Long,
        var firstName: String,
)

interface UserRepository : CoroutineCrudRepository<User, Long>

@Component
class UserHandler(private val repository: UserRepository) {

    private val client: WebClient = WebClient.create()

    suspend fun getAll(req: ServerRequest): ServerResponse {
        val flow = repository.findAll()
        return ok().bodyAndAwait(flow)
    }

    suspend fun getOne(req: ServerRequest): ServerResponse {
        val person = repository.findById(req.pathVariable("id").toLong())
        return if (person == null) notFound().buildAndAwait()
        else ok().bodyValueAndAwait(person)
    }

    suspend fun save(req: ServerRequest): ServerResponse =
            ok().bodyValueAndAwait(repository.save(req.awaitBody(User::class)))

    /*private suspend fun withDetails(user: User): UserWithDetails = coroutineScope {
        val asyncDetail1 = async {
            client.get().uri("/userdetail1/${user.firstName}")
                    .accept(APPLICATION_JSON)
                    .awaitExchange().awaitBody<UserDetail1>()
        }
        val asyncDetail2 = async {
            client.get().uri("/userdetail2/${user.firstName}")
                    .accept(APPLICATION_JSON)
                    .awaitExchange().awaitBody<UserDetail2>()
        }
        UserWithDetails(user, asyncDetail1.await(), asyncDetail2.await())
    }*/
}

@Configuration
class UserRoutes {

    @Bean
    fun router(handler: UserHandler) = coRouter {
        GET("/users", handler::getAll)
        GET("/users/{id}", handler::getOne)
        POST("/users", handler::save)
    }
}
