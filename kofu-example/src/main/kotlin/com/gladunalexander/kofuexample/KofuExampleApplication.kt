package com.gladunalexander.kofuexample

import org.springframework.boot.WebApplicationType
import org.springframework.fu.kofu.application

fun main() {
    app.run()
}

val app = application(WebApplicationType.REACTIVE) {
    configurationProperties<SampleProperties>("sample")
    enable(dataConfig)
    enable(webConfig)
}
