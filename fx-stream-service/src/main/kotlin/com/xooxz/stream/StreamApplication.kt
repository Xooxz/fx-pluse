package com.xooxz.stream

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.xooxz.stream", "com.xooxz.common"])
class StreamApplication

fun main(args: Array<String>) {
    runApplication<StreamApplication>(*args)
}
