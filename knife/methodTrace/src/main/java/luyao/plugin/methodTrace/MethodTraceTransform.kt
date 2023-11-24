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
internal interface MethodTraceParameters: InstrumentationParameters {
//    @get:Input
//    val buildType: Property<String>
}

internal abstract class MethodTraceTransform: AsmClassVisitorFactory<MethodTraceParameters> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
//        val buildType = parameters.get().buildType.get()
        return MethodTraceClassVisitor(nextClassVisitor)
    }

    /**
     * 这里返回 true 的 class 才会被 classVisitor 访问
     */
    override fun isInstrumentable(classData: ClassData): Boolean {
        return true
    }
}