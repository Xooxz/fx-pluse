package com.xooxz.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.xooxz.auth", "com.xooxz.common"])
class AuthApplication

fun main(args: Array<String>) {
    runApplication<AuthApplication>(*args)
}
