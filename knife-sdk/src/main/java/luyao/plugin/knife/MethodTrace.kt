package luyao.plugin.knife


/**
 * Description:
 * Author: luyao
 * Date: 2023/11/24 10:42
 */
@Target(AnnotationTarget.FUNCTION)
annotation class MethodTrace(
    val traceTime: Boolean = true,
    val traceParamsAndReturnValue: Boolean = true
)
