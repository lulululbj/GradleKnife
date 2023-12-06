package luyao.plugin.methodTrace

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Description:
 * Author: luyao
 * Date: 2023/11/24 10:27
 */
class MethodTracePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        print("MethodTracePlugin apply")

        project.extensions.create(
            "methodTrace",
            MethodTraceConfig::class.java
        )

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->

            val methodTraceConfig = project.extensions.findByType(MethodTraceConfig::class.java) ?: MethodTraceConfig()

            variant.instrumentation.transformClassesWith(
                MethodTraceTransform::class.java,
                InstrumentationScope.ALL
            ) { params ->
                val config = MethodTraceConfigParam(
                    methodTraceConfig.traceTime,
                    methodTraceConfig.traceParamsAndReturnValue,
                    methodTraceConfig.traceMethods)
                params.config.set(config)
            }
            variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS)
        }
    }
}