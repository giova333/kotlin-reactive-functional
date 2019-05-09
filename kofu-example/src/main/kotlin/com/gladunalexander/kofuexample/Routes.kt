package com.gladunalexander.kofuexample

import org.springframework.web.reactive.function.server.router

/**
 * Created by Alexander Gladun on 2019-05-09.
 */
fun routes(userHandler: UserHandler) = router {
    GET("/", userHandler::listView)
    GET("/api/user", userHandler::listApi)
    GET("/conf", userHandler::conf)
}