package com.gladunalexander.kofuexample

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by Alexander Gladun on 2019-05-09.
 */

@Document
data class User(
        @Id val login: String,
        val firstname: String,
        val lastname: String
)
