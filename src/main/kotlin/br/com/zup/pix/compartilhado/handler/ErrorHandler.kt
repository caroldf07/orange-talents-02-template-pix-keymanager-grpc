package br.com.zup.pix.compartilhado.handler

import br.com.zup.pix.compartilhado.exception.interceptor.ExceptionHandlerInterceptor
import io.micronaut.aop.Around
import io.micronaut.context.annotation.Type
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.TYPE

@MustBeDocumented
@Target(CLASS, TYPE)
@Retention(RUNTIME)
@Type(ExceptionHandlerInterceptor::class)
@Around //Essa é a anotation que vai vincular com o interceptor
annotation class ErrorHandler
