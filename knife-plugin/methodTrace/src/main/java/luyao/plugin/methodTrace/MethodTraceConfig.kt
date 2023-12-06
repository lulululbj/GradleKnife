package luyao.plugin.methodTrace

import java.io.Serializable

/**
 * Description:
 * Author: luyao
 * Date: 2023/11/29 16:42
 */
open class MethodTraceConfig(
    var traceTime: Boolean = true,
    var traceParamsAndReturnValue: Boolean = true,
    var traceMethods: List<TraceMethod> = emptyList()
)

class MethodTraceConfigParam(
    val traceTime: Boolean = true,
    val traceParamsAndReturnValue: Boolean = true,
    val traceMethods: List<TraceMethod> = emptyList()
) : Serializable {
    override fun toString(): String {
        return "MethodTraceConfigParam(traceTime=$traceTime, traceParamsAndReturnValue=$traceParamsAndReturnValue)"
    }
}

data class TraceMethod(
    val className: String,
    val methodName: String,
) : Serializable