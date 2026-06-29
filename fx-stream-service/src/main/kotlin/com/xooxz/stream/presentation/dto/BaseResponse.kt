package com.xooxz.stream.presentation.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.io.Serializable
import java.sql.Timestamp
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BaseResponse<T>(
    val success: Boolean,
    val message: String,
    val errorCode: String?,
    val data: T? = null,
    val timestamp: Long = Timestamp.valueOf(LocalDateTime.now()).time
) : Serializable {

    companion object {
        fun <T> succeed(data: T? = null): BaseResponse<T> =
            BaseResponse(success = true, message = "success", errorCode = null, data = data)

        fun <T> succeed(message: String, data: T? = null): BaseResponse<T> =
            BaseResponse(success = true, message = message, errorCode = null, data = data)

        fun <T> failed(message: String): BaseResponse<T> =
            BaseResponse(success = false, message = message, errorCode = null, data = null)

        fun <T> failed(message: String, code: String): BaseResponse<T> =
            BaseResponse(success = false, message = message, errorCode = code, data = null)
    }

}