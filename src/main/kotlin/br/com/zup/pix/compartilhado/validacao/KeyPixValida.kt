package br.com.zup.pix.compartilhado

import br.com.zup.pix.dominio.NovaChavePixDto
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.TYPE
import kotlin.reflect.KClass

@MustBeDocumented
@Target(CLASS, TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = [KeyPixValidator::class])
annotation class KeyPixValida(
    val message: String = "chave Pix inválida (\${validatedValue.tipoChave})",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = [],
)

@Singleton
class KeyPixValidator : ConstraintValidator<KeyPixValida, NovaChavePixDto> {
    override fun isValid(value: NovaChavePixDto?, context: ConstraintValidatorContext?): Boolean {
        if (value?.tipoChave == null) {
            return false
        }

        return value.tipoChave.valida(value.valorChave)
    }

}
