package luyao.plugin.methodTrace

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.commons.LocalVariablesSorter
import java.lang.invoke.MethodType

/**
 * Description:
 * Author: luyao
 * Date: 2023/12/1 13:22
 */
object MethodTraceUtil {

    /**
     * 方法开始记时
     */
    fun startTraceTime(mv: MethodVisitor, name: String) {
        // name 压入操作数栈
        mv.visitLdcInsn(name)
        // 访问 MethodTimeCounter.start()
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "luyao/plugin/knife/MethodTimeCounter",
            "start",
            "(Ljava/lang/String;)V",
            false
        )
    }

    /**
     * 方法结束记时
     */
    fun endTraceTime(mv: MethodVisitor, name: String) {
        // name 压入操作数栈
        mv.visitLdcInsn(name)
        // 访问 MethodTimeCounter.end()
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "luyao/plugin/knife/MethodTimeCounter",
            "end",
            "(Ljava/lang/String;)V",
            false
        )
    }

    /**
     * 获取方法参数和返回值
     */
    fun traceParamsAndReturnValue(
        mv: MethodVisitor,
        access: Int,
        className: String,
        name: String,
        descriptor: String,
        localVariablesSorter: LocalVariablesSorter,
        opcode: Int
    ) {
        // 创建 ArrayList，并将引用压入操作数栈
        mv.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList")
        // 复制栈顶数据并压入栈顶
        mv.visitInsn(Opcodes.DUP)
        // 执行 ArrayList 的构造方法，并且弹出栈顶的一个实例引用
        mv.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/util/ArrayList",
            "<init>",
            "()V",
            false
        )
        val localVariablesIdentifier = localVariablesSorter.newLocal(Type.getType(List::class.java))
        // 将栈顶的另一个 ArrayList 的实例引用存入本地变量表
        mv.visitVarInsn(Opcodes.ASTORE, localVariablesIdentifier)

        // 开始填充 ArrayList
        // static 方法中，第一个参数就是是方法参数
        // 非 static 方法中，第一个参数是 this
        val isStatic = (access and Opcodes.ACC_STATIC) != 0
        var cursor = if (isStatic) 0 else 1

        val methodType = Type.getMethodType(descriptor)
        methodType.argumentTypes.forEach { argumentType ->
            // 取出创建的 ArrayList，压入操作数栈
            mv.visitVarInsn(AdviceAdapter.ALOAD, localVariablesIdentifier)
            // 根据参数类型，调用不同的 LOAD 方法
            val opCode = argumentType.getOpcode(Opcodes.ILOAD)
            // 将本地变量表中的方法参数压入操作数栈
            mv.visitVarInsn(opCode, cursor)
            if (argumentType.sort in Type.BOOLEAN..Type.DOUBLE) {
                // 自动装箱
                typeCastToObject(mv, argumentType)
            }
            cursor += argumentType.size
            // 调用 ArrayList 的 add 方法
            mv.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                "java/util/List",
                "add",
                "(Ljava/lang/Object;)Z",
                true
            )
            // 弹出栈顶
            mv.visitInsn(Opcodes.POP)
        }

        // 获取返回值，统一使用字符串类型
        when (opcode) {
            in Opcodes.IRETURN..Opcodes.DRETURN -> {
                loadReturnData(mv, methodType)
            }

            Opcodes.ARETURN -> {
                mv.visitInsn(Opcodes.DUP)
            }

            Opcodes.RETURN -> {
                mv.visitLdcInsn("void")
            }

            Opcodes.ATHROW -> {
                mv.visitLdcInsn("throw")
            }
        }

        val paramTypes = methodType.argumentTypes.joinToString(", ") { it.descriptor }
        val returnType = methodType.returnType.descriptor

        mv.visitLdcInsn(className)     // 类名
        mv.visitLdcInsn(name)          // 方法名
        mv.visitLdcInsn("[$paramTypes]")    // 参数类型列表
        mv.visitVarInsn(Opcodes.ALOAD, localVariablesIdentifier) // 参数列表
        mv.visitLdcInsn(returnType)    // 返回值类型

        // 调用 MethodParamTrace.onMethodTraced() 打印日志
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "luyao/plugin/knife/MethodParamTrace",
            "onMethodTraced",
            "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/lang/String;)V",
            false
        )
    }

    private fun loadReturnData(mv: MethodVisitor, methodType: Type) {
        // 复制操作数栈顶，判断是一个 slot 还是两个 slot
        mv.visitInsn(if (methodType.returnType.size == 1) Opcodes.DUP else Opcodes.DUP2)
        // 自动装箱
        typeCastToObject(mv, methodType.returnType)
    }

    private fun typeCastToObject(mv: MethodVisitor, type: Type) {
        when (type) {
            Type.INT_TYPE -> {
                mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Integer",
                    "valueOf",
                    "(I)Ljava/lang/Integer;",
                    false
                )
            }

            Type.CHAR_TYPE -> {
                mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Character",
                    "valueOf",
                    "(C)Ljava/lang/Character;",
                    false
                )
            }

            Type.BYTE_TYPE -> {
                mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Byte",
                    "valueOf",
                    "(B)Ljava/lang/Byte;",
                    false
                )
            }

            Type.BOOLEAN_TYPE -> {
                mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Boolean",
                    "valueOf",
                    "(Z)Ljava/lang/Boolean;",
                    false
                )
            }

            Type.SHORT_TYPE -> {
                mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Short",
                    "valueOf",
                    "(S)Ljava/lang/Short;",
                    false
                )
            }

            Type.FLOAT_TYPE -> {
                mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Float",
                    "valueOf",
                    "(F)Ljava/lang/Float;",
                    false
                )
            }

            Type.LONG_TYPE -> {
                mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Long",
                    "valueOf",
                    "(J)Ljava/lang/Long;",
                    false
                )
            }

            Type.DOUBLE_TYPE -> {
                mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Double",
                    "valueOf",
                    "(D)Ljava/lang/Double;",
                    false
                )
            }
        }
    }
}