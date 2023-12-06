package luyao.plugin.knife

import android.util.Log

/**
 * Description:
 * Author: luyao
 * Date: 2023/11/29 15:22
 */
object MethodParamTrace {

    @JvmStatic
    fun onMethodEnter(className: String, methodName: String, paramList: List<Any>) {
        Log.e("MethodTrace", "onMethodEnter: $className $methodName $paramList")
    }

    @JvmStatic
    fun onMethodExit(
        response: Any? = null,
        className: String,
        methodName: String,
        parameterTypes: String,
        returnType: String
    ) {
        Log.e(
            "MethodTrace",
            "onMethodExit: $className $methodName $parameterTypes $returnType $response"
        )
    }

    @JvmStatic
    fun onMethodTraced(
        response: Any? = null,
        className: String,
        methodName: String,
        parameterTypes: String,
        paramList: List<Any>,
        returnType: String,
    ) {
        Log.e(
            "MethodTrace",
            "onMethodTraced: $className\$$methodName \n参数类型列表：$parameterTypes \n参数列表：$paramList \n返回值类型：$returnType \n返回值：$response"
        )
    }
}