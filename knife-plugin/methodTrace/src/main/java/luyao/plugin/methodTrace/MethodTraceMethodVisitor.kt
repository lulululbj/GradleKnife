package luyao.plugin.methodTrace

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

/**
 * Description:
 * Author: luyao
 * Date: 2023/11/24 15:04
 */
class MethodTraceMethodVisitor(
    private val className: String,
    mv: MethodVisitor,
    access: Int,
    name: String,
    private val descriptor: String,
    private val config: MethodTraceConfigParam
) : AdviceAdapter(Opcodes.ASM9, mv, access, name, descriptor) {

    private var needTrace = false
    private var traceTime = config.traceTime
    private var traceParamsAndReturnValue = config.traceParamsAndReturnValue

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        if (descriptor == "Lluyao/plugin/knife/MethodTrace;") {
            println("visitAnnotation: descriptor:$descriptor isVisible:$visible")
            needTrace = true
            return object : AnnotationVisitor(Opcodes.ASM9) {
                override fun visit(name: String?, value: Any?) {
                    // 获取 MethodTrace 注解参数
                    println("visit: name:$name value:$value")
                    if (name == "traceTime") {
                        traceTime = value as Boolean
                    } else if (name == "traceParamsAndReturnValue") {
                        traceParamsAndReturnValue = value as Boolean
                    }
                }
            }
        }
        return super.visitAnnotation(descriptor, visible)
    }

    override fun onMethodEnter() {
        super.onMethodEnter()

        // 检测 extensions 中的配置
        if (config.traceMethods.any {
                it.className == className.replace(
                    "/",
                    "."
                ) && it.methodName == name
            }) {
            needTrace = true
        }

        if (!needTrace) return
        println("onMethodEnter: $className $name")

        if (traceTime) {
            // 开始方法记时
            MethodTraceUtil.startTraceTime(mv, "$className\$$name")
        }
    }

    override fun onMethodExit(opcode: Int) {

        if (!needTrace) return
        println("onMethodExit: $name")

        if (traceTime) {
            // 结束方法记时
            MethodTraceUtil.endTraceTime(mv, "$className\$$name")
        }

        if (traceParamsAndReturnValue) {
            // 获取方法参数和返回值
            MethodTraceUtil.traceParamsAndReturnValue(
                mv,
                access,
                className,
                name,
                descriptor,
                this,
                opcode
            )
        }

        super.onMethodExit(opcode)
    }
}