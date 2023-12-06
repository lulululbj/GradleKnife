package luyao.plugin.methodTrace

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode

/**
 * Description:
 * Author: luyao
 * Date: 2023/12/5 16:05
 */
class MethodTraceClassNode(private val nextClassVisitor: ClassVisitor, private val config: MethodTraceConfigParam) : ClassNode(Opcodes.ASM9) {

    override fun visitEnd() {
        super.visitEnd()

       methods.forEach { methodNode ->
           methodNode.instructions.forEach { abstractInsnNode ->
               if (abstractInsnNode is MethodInsnNode && abstractInsnNode.owner == "java/lang/System" && abstractInsnNode.name == "loadLibrary") {
                   println("find loadLibrary: ${abstractInsnNode.owner} ${abstractInsnNode.name} ${abstractInsnNode.desc}")
                   val node = abstractInsnNode.previous
                   if (node is LdcInsnNode) {
                          println("find loadLibrary: ${node.cst}")
                   }
               }
           }
       }
        accept(nextClassVisitor)
    }
}