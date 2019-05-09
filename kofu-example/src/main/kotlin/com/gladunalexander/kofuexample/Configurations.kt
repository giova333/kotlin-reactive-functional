package com.gladunalexander.kofuexample

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.fu.kofu.configuration
import org.springframework.fu.kofu.mongo.reactiveMongodb
import org.springframework.fu.kofu.webflux.mustache
import org.springframework.fu.kofu.webflux.webFlux

/**
 * Created by Alexander Gladun on 2019-05-09.
 */

val dataConfig = configuration {
    beans {
        bean<UserRepository>()
    }
    listener<ApplicationReadyEvent> {
        ref<UserRepository>().init()
    }
    reactiveMongodb {
        embedded()
    }
}

val webConfig = configuration {
    beans {
        bean<UserHandler>()
        bean(::routes)
    }
    webFlux {
        port = if (profiles.contains("test")) 8181 else 8080
        mustache()
        codecs {
            string()
            jackson()
        }
    }
}