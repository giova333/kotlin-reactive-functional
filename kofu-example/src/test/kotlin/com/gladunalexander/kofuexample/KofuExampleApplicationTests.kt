package com.gladunalexander.kofuexample

import org.junit.Test
import org.springframework.context.ConfigurableApplicationContext


class KofuExampleApplicationTests {

    private val context: ConfigurableApplicationContext = app.run(profiles = "test")

    @Test
    fun contextLoads() {
        context.beanDefinitionNames.forEach { println(it) }
    }

}
