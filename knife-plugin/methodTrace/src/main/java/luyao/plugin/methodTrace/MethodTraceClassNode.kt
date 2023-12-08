package luyao.plugin.methodTrace

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.LocalVariableNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode

/**
 * Description:
 * Author: luyao
 * Date: 2023/12/5 16:05
 */
class MethodTraceClassNode(
    private val nextClassVisitor: ClassVisitor,
    private val className: String,
    private val config: MethodTraceConfigParam
) : ClassNode(Opcodes.ASM9) {

    private var needTrace = false
    private var traceTime = config.traceTime
    private var traceParamsAndReturnValue = config.traceParamsAndReturnValue

    override fun visitEnd() {
        super.visitEnd()

        methods.forEach { methodNode ->
            // 过滤 init，clinit，抽象方法，native 方法
            if (methodNode.name == "<init>" || methodNode.name == "<clinit>"
                || methodNode.access and Opcodes.ACC_ABSTRACT != 0
                || methodNode.access and Opcodes.ACC_NATIVE != 0
            ) {
                return@forEach
            }


            // 处理 MethodTrace 注解
            methodNode.visibleAnnotations?.forEach { annotationNode ->
                if (annotationNode.desc == "Lluyao/plugin/knife/MethodTrace;") {
                    needTrace = true
                    annotationNode.values?.forEachIndexed { index, value ->
                        if (value is String && value == "traceTime") {
                            traceTime = annotationNode.values[index + 1] as Boolean
                        } else if (value is String && value == "traceParamsAndReturnValue") {
                            traceParamsAndReturnValue = annotationNode.values[index + 1] as Boolean
                        }
                    }
                }
            }

            if (!needTrace) {
                // 判断是否在 extension 配置的方法列表中
                if (!config.traceMethods.none { traceMethod ->
                        traceMethod.className == className && traceMethod.methodName == methodNode.name
                    }) {
                    println("class $className method ${methodNode.name} in extension traceMethods")
                    needTrace = true
                }
            }

            if (needTrace) {
                println("start trace class $className method ${methodNode.name}")
                if (traceTime) {
                    val traceTag = "$className\$$name"
                    methodNode.instructions?.run {

                        // 在头部插入 MethodTrace.startMethodTrace()
                        insert(InsnList().apply {
                            add(LdcInsnNode(traceTag))
                            add(
                                MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "luyao/plugin/knife/MethodTimeCounter",
                                    "start",
                                    "(Ljava/lang/String;)V",
                                    false
                                )
                            )
                        })

                        // 在尾部插入 MethodTrace.endMethodTrace()
                        // 注意要在 XRETURN 或者 ATHROW 之前插入
                        forEach { insnNode ->
                            val opCode = insnNode.opcode
                            if (opCode in Opcodes.IRETURN..Opcodes.RETURN
                                || opCode == Opcodes.ATHROW
                            ) {
                                insertBefore(insnNode, InsnList().apply {
                                    add(LdcInsnNode(traceTag))
                                    add(
                                        MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            "luyao/plugin/knife/MethodTimeCounter",
                                            "end",
                                            "(Ljava/lang/String;)V",
                                            false
                                        )
                                    )
                                })

                                if (traceParamsAndReturnValue) {
                                    insertBefore(
                                        insnNode,
                                        generateTraceParamsAndReturnValueInsnList(
                                            methodNode,
                                            insnNode.opcode
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            needTrace = false
        }

        accept(nextClassVisitor)
    }

    private fun generateTraceParamsAndReturnValueInsnList(
        methodNode: MethodNode,
        opcode: Int
    ): InsnList {

        return InsnList().apply {
            add(TypeInsnNode(Opcodes.NEW, "java/util/ArrayList"))
            add(InsnNode(Opcodes.DUP))
            add(
                MethodInsnNode(
                    Opcodes.INVOKESPECIAL,
                    "java/util/ArrayList",
                    "<init>",
                    "()V",
                    false
                )
            )

            // 这句注释掉仍可以达到效果，且最后的 class 文件对应方法的局部变量表看起来缺失一个 slot
            methodNode.localVariables.add(
                LocalVariableNode(
                    "knife_param_list",
                    "Ljava/util/List;",
                    null,
                    LabelNode(),
                    LabelNode(),
                    methodNode.localVariables.size
                )
            )
            val varIndex = methodNode.localVariables.size
            add(VarInsnNode(Opcodes.ASTORE, varIndex))

            val isStatic = methodNode.access and Opcodes.ACC_STATIC != 0
            var cursor = if (isStatic) 0 else 1

            val methodType = Type.getMethodType(methodNode.desc)
            methodType.argumentTypes.forEach { argumentType ->
                println("${methodNode.name} argument: ${argumentType.internalName} ${argumentType.descriptor}")
                add(VarInsnNode(argumentType.getOpcode(Opcodes.ILOAD), varIndex))
                val opCode = argumentType.getOpcode(Opcodes.ILOAD)
                add(VarInsnNode(opCode, cursor))
                if (argumentType.sort in Type.BOOLEAN..Type.DOUBLE) {
                    typeCastToObject(this, argumentType)
                }
                cursor += argumentType.size
                add(MethodInsnNode(
                    Opcodes.INVOKEINTERFACE,
                    "java/util/List",
                    "add",
                    "(Ljava/lang/Object;)Z",
                    true
                ))
                add(InsnNode(Opcodes.POP))
            }

            when (opcode) {
                in Opcodes.IRETURN..Opcodes.DRETURN -> {
                    loadReturnData(this, methodType)
                }

                Opcodes.ARETURN -> {
                    add(InsnNode(Opcodes.DUP))
                }

                Opcodes.RETURN -> {
                    add(LdcInsnNode("void"))
                }

                Opcodes.ATHROW -> {
                    add(LdcInsnNode("throw"))
                }
            }

            val paramTypes = methodType.argumentTypes.joinToString(", ") { it.descriptor }
            val returnType = methodType.returnType.descriptor

            add(LdcInsnNode(className))
            add(LdcInsnNode(methodNode.name))
            add(LdcInsnNode("[$paramTypes]"))
            add(VarInsnNode(Opcodes.ALOAD, varIndex))
            add(LdcInsnNode(returnType))

            add(MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "luyao/plugin/knife/MethodParamTrace",
                "onMethodTraced",
                "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/lang/String;)V",
                false
            ))

        }
    }

    private fun loadReturnData(insnList: InsnList, methodType: Type) {
        // 复制操作数栈顶，判断是一个 slot 还是两个 slot
        insnList.add(InsnNode(if (methodType.returnType.size == 1) Opcodes.DUP else Opcodes.DUP2))
        // 自动装箱
        typeCastToObject(insnList, methodType.returnType)
    }

    private fun typeCastToObject(insnList: InsnList, type: Type) {
        insnList.run {
            when (type) {
                Type.INT_TYPE -> {
                    add(
                        MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "java/lang/Integer",
                            "valueOf",
                            "(I)Ljava/lang/Integer;",
                            false
                        )
                    )
                }

                Type.CHAR_TYPE -> {
                    add(
                        MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "java/lang/Character",
                            "valueOf",
                            "(C)Ljava/lang/Character;",
                            false
                        )
                    )
                }

                Type.BYTE_TYPE -> {
                    add(
                        MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "java/lang/Byte",
                            "valueOf",
                            "(B)Ljava/lang/Byte;",
                            false
                        )
                    )
                }

                Type.BOOLEAN_TYPE -> {
                    add(
                        MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "java/lang/Boolean",
                            "valueOf",
                            "(Z)Ljava/lang/Boolean;",
                            false
                        )
                    )
                }

                Type.SHORT_TYPE -> {
                    add(
                        MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "java/lang/Short",
                            "valueOf",
                            "(S)Ljava/lang/Short;",
                            false
                        )
                    )
                }

                Type.FLOAT_TYPE -> {
                    add(
                        MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "java/lang/Float",
                            "valueOf",
                            "(F)Ljava/lang/Float;",
                            false
                        )
                    )
                }

                Type.LONG_TYPE -> {
                    add(
                        MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "java/lang/Long",
                            "valueOf",
                            "(J)Ljava/lang/Long;",
                            false
                        )
                    )
                }

                Type.DOUBLE_TYPE -> {
                    add(
                        MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "java/lang/Double",
                            "valueOf",
                            "(D)Ljava/lang/Double;",
                            false
                        )
                    )
                }

                else -> {}
            }
        }

    }
}