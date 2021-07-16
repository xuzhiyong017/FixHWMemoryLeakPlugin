package com.sky.asmplugin.transform

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.*


/**
 * @author: xuzhiyong
 * @date: 2021/7/15  上午9:41
 * @Email: 18971269648@163.com
 * @description:
 */

fun injectFixLeakCode(mv: MethodVisitor,needVisitorMethod:Boolean = false){

    //方法执行后
    mv.visitVarInsn(ALOAD,0)
    mv.visitMethodInsn(INVOKESTATIC,"com/sky/asmfixleak/util/FixMemLeak","fixHWPhoneMemoryLeak","(Landroid/app/Activity;)V",false)
}

fun injectFixLeakMethodOndestroy(
    classVisitor: ClassVisitor,
    className: String?,
    superName: String?
){
    val mv = classVisitor.visitMethod(ACC_PROTECTED, "onDestroy", "()V", null, null)
    mv.visitCode()
//    val l0 = Label()
//    mv.visitLabel(l0)
//    mv.visitLineNumber(5, l0)
    mv.visitVarInsn(ALOAD, 0)
    mv.visitMethodInsn(INVOKESPECIAL, "${superName}", "onDestroy", "()V", false)
//    val l1 = Label()
//    mv.visitLabel(l1)
//    mv.visitLineNumber(6, l1)
    mv.visitVarInsn(ALOAD,0)
    mv.visitMethodInsn(INVOKESTATIC,"com/sky/asmfixleak/util/FixMemLeak","fixHWPhoneMemoryLeak","(Landroid/app/Activity;)V",false)
    mv.visitInsn(RETURN)
//    val l2 = Label()
//    mv.visitLabel(l2)
//    mv.visitLocalVariable("this", "L${className};", null, l0, l2, 0)
    mv.visitMaxs(1, 1)
    mv.visitEnd()
}