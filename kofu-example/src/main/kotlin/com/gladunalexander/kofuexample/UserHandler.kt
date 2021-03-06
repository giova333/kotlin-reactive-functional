package com.gladunalexander.kofuexample

import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

/**
 * Created by Alexander Gladun on 2019-05-09.
 */
class UserHandler(
        private val repository: UserRepository,
        private val configuration: SampleProperties) {

    fun listApi(request: ServerRequest) = ServerResponse
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(repository.findAll(), User::class.java)

    fun listView(request: ServerRequest) = ServerResponse
            .ok()
            .render("users", mapOf("users" to repository.findAll()))


    fun conf(request: ServerRequest) = ServerResponse
            .ok()
            .syncBody(configuration.message)

}