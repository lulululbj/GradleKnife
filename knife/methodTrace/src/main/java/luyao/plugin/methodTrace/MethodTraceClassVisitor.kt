package luyao.plugin.methodTrace

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Description:
 * Author: luyao
 * Date: 2023/11/24 11:06
 */
class MethodTraceClassVisitor(cv: ClassVisitor) : ClassVisitor(Opcodes.ASM9, cv) {


    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        var mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (name == "<init>" || name == "<clinit>"
            || access and Opcodes.ACC_ABSTRACT != 0
            || access and Opcodes.ACC_NATIVE != 0
        ) {
            return mv
        }
        mv = MethodTraceMethodVisitor(mv, access, name, descriptor)
        return mv
    }

}