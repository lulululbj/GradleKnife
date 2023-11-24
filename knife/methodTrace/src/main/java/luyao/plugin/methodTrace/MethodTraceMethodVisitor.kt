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
class MethodTraceMethodVisitor(mv: MethodVisitor, access: Int, name: String, descriptor: String) :
    AdviceAdapter(Opcodes.ASM9, mv, access, name, descriptor) {

    private var needTrace = false

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        if (descriptor == "Lluyao/plugin/knife/api/MethodTrace;") {
            println("visitAnnotation: descriptor:$descriptor isVisible:$visible")
            needTrace = true
        }
        return super.visitAnnotation(descriptor, visible)
    }

    override fun onMethodEnter() {
        super.onMethodEnter()
        if (!needTrace) return
        println("onMethodEnter: $name")

        mv.visitLdcInsn(name)
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "luyao/gradle/knife/TimeCounter",
            "start",
            "(Ljava/lang/String;)V",
            false
        )
    }

    override fun onMethodExit(opcode: Int) {
//        super.onMethodExit(opcode)
        if (!needTrace) return
        println("onMethodExit: $name")

        mv.visitLdcInsn(name)
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "luyao/gradle/knife/TimeCounter",
            "end",
            "(Ljava/lang/String;)V",
            false
        )
    }
}