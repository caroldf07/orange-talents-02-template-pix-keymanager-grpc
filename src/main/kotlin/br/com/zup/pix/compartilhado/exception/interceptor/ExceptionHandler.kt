package br.com.zup.pix.compartilhado.exception.interceptor


import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto

interface ExceptionHandler<in E : Exception> {

    //Mapeamento do handler com a exception
    fun handle(e: E): StatusWithDetails

    /*Verifica se o handler consegue lidar com a exceção mesmo. Isso ocorre porque, por questão de nomeclatura, o próprio
    Micronaut já vincula o handler com a exception e aqui fazemos uma verificação dentro da handler*/
    fun supports(e: Exception): Boolean

    data class StatusWithDetails(val status: Status, val metadata: io.grpc.Metadata = Metadata()) {
        constructor(se: StatusRuntimeException) : this(se.status, se.trailers ?: Metadata())
        constructor(sp: com.google.rpc.Status) : this(StatusProto.toStatusRuntimeException(sp))

        fun asRuntimeException(): StatusRuntimeException {
            return status.asRuntimeException(metadata)
        }
    }
}
