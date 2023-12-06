package luyao.plugin.methodTrace

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor

/**
 * Description:
 * Author: luyao
 * Date: 2023/11/24 10:58
 */
internal interface MethodTraceConfigParameters : InstrumentationParameters {
    @get:Input
    val config: Property<MethodTraceConfigParam>
}

internal abstract class MethodTraceTransform : AsmClassVisitorFactory<MethodTraceConfigParameters> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return MethodTraceClassVisitor(nextClassVisitor, parameters.get().config.get())
//        return MethodTraceClassNode(nextClassVisitor, parameters.get().config.get())
    }

    /**
     * 这里返回 true 的 class 才会被 classVisitor 访问
     */
    override fun isInstrumentable(classData: ClassData): Boolean {
        return true
    }
}