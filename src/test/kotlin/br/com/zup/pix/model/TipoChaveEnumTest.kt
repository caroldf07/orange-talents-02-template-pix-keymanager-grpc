package br.com.zup.pix.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TipoChaveEnumTest {

    /*
    * 1 - happy path - ok
    * 2 - valor nulo ou vazio - ok
    * 3 - formato inválido - ok
    * 4 - formato válido, porém não é documento válido - cpf
    * */

    @Nested
    inner class CPF {
        @Test
        @DisplayName(value = "não deve validar quando não for dado o cpf")
        fun `não deve ser válido quando não for dado o cpf`() {

            //cenário

            //ação
            with(TipoChaveEnum.CPF) {
                //validação
                assertFalse(valida(chave = null))
                assertFalse(valida(chave = ""))
            }
        }

        @Test
        @DisplayName(value = "deve validar documento válido")
        fun `deve validar documento válido`() {
            //cenário

            //ação
            with(TipoChaveEnum.CPF) {
                //validação
                assertTrue(valida(chave = "556.104.490-73"))
                assertTrue(valida(chave = "55610449073"))
            }

        }

        @Test
        @DisplayName(value = "não deve validar um cpf cujo número não é válido pela receita federal")
        fun `não deve validar um cpf cujo número não é válido pela receita federal`() {
            //cenário

            //ação
            with(TipoChaveEnum.CPF) {
                //validação
                assertFalse(valida(chave = "123.456.789-00"))
                assertFalse(valida(chave = "12345678900"))
            }
        }
    }

    @Nested
    inner class CELULAR {

        @Test
        @DisplayName("deve validar o número de celular quando é um formato válido")
        fun `deve validar o número de celular quando é um formato válido`() {
            //cenário

            //ação
            with(TipoChaveEnum.CELULAR) {
                //validação
                assertTrue(valida(chave = "+5511999999999"))
            }
        }

        @Test
        @DisplayName("não deve validar o número de celular quando o formato não é válido")
        fun `não deve validar o número de celular quando o formato não é válido`() {

            //cenário

            //ação
            with(TipoChaveEnum.CELULAR) {
                //validação
                assertFalse(valida(chave = "999999999"))
            }
        }

        @Test
        @DisplayName("não deve validar quando é um valor vazio e ou nulo")
        fun `não deve validar quando é um valor vazio e ou nulo`() {
            //cenário

            //ação
            with(TipoChaveEnum.CELULAR) {
                //validação
                assertFalse(valida(chave = ""))
                assertFalse(valida(chave = null))
            }
        }
    }

    @Nested
    inner class EMAIL {

        @Test
        @DisplayName("deve validar quando é um email válido")
        fun `deve validar quando é um email válido`() {
            //cenário

            //ação
            with(TipoChaveEnum.EMAIL) {
                //validação
                assertTrue(valida(chave = "email@email.com"))
            }
        }

        @Test
        @DisplayName("não deve validar quando for vazio e ou nulo")
        fun `não deve validar quando for vazio e ou nulo`() {
            //cenário

            //ação
            with(TipoChaveEnum.EMAIL) {
                //validação
                assertFalse(valida(chave = ""))
                assertFalse(valida(chave = null))
            }
        }

        @Test
        @DisplayName("não deve validar quando email tiver formato inválido")
        fun `não deve validar quando email tiver formato inválido`() {
            //cenário

            //ação
            with(TipoChaveEnum.EMAIL) {
                //validação
                assertFalse(valida(chave = "email"))
            }
        }
    }

    @Nested
    inner class ALEATORIA {

        @Test
        @DisplayName("deve retornar validar quando campo valor branco ou nulo")
        fun `deve validar quando campo valor branco ou nulo`() {
            //cenário

            //ação
            with(TipoChaveEnum.ALEATORIA) {
                //validação
                assertTrue(valida(chave = ""))
                assertTrue(valida(chave = null))
            }
        }

        @Test
        @DisplayName("não deve validar quando campo tiver algum dado")
        fun `não deve validar quando campo tiver algum dado`() {
            //cenário

            //ação
            with(TipoChaveEnum.ALEATORIA) {
                //validação
                assertFalse(valida(chave = "string qualquer"))
            }
        }
    }
}