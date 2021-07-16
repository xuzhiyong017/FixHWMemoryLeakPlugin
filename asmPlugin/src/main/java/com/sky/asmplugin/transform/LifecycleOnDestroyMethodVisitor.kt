package com.sky.asmplugin.transform

import com.sky.asmplugin.transform.injectFixLeakCode
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @author: xuzhiyong
 * @date: 2021/7/13  上午9:42
 * @Email: 18971269648@163.com
 * @description:
 */
class LifecycleOnDestroyMethodVisitor(mv: MethodVisitor?) : MethodVisitor(Opcodes.ASM9, mv) {

    override fun visitInsn(opcode: Int) {
        if (opcode == Opcodes.RETURN) {
            //方法执行后
            injectFixLeakCode(mv)
        }
        super.visitInsn(opcode)
    }
}