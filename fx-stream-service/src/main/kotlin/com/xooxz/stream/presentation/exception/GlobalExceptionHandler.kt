package com.xooxz.stream.presentation.exception

import com.xooxz.stream.domain.exception.RateNotFoundException
import com.xooxz.stream.domain.exception.UnsupportedCurrencyException
import com.xooxz.stream.presentation.dto.BaseResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(basePackages = ["com.xooxz.stream.presentation.controller"])
class GlobalExceptionHandler {

    companion object {
        private const val DEFAULT_ERROR_MESSAGE = "오류가 발생했습니다."
    }

    @ExceptionHandler(UnsupportedCurrencyException::class)
    fun handleUnsupportedCurrencyException(
        ex: UnsupportedCurrencyException
    ): ResponseEntity<BaseResponse<Void>> {

        return ResponseEntity.badRequest()
            .body(
                BaseResponse.Companion.failed(
                    message = ex.message ?: DEFAULT_ERROR_MESSAGE,
                    code = "INVALID_CURRENCY"
                )
            )
    }

    @ExceptionHandler(RateNotFoundException::class)
    fun handleRateNotFoundException(
        ex: RateNotFoundException
    ): ResponseEntity<BaseResponse<Void>> {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(
                BaseResponse.failed(
                    message = ex.message ?: DEFAULT_ERROR_MESSAGE,
                    code = "RATE_NOT_FOUND"
                )
            )
    }

}