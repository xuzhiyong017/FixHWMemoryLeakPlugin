package com.sky.asmplugin.transform

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @author: xuzhiyong
 * @date: 2021/7/13  上午9:42
 * @Email: 18971269648@163.com
 * @description:
 */
class FixLeakClassVisitor(cv: ClassVisitor) : ClassVisitor(Opcodes.ASM9,cv) {

    var className:String? = null
    var superName:String? = null
    private var hasDestroyMethod:Boolean = false
    private var needInsertCode = false

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        this.className = name
        this.superName = superName
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val mv = cv.visitMethod(access, name, descriptor, signature, exceptions)
        if("onDestroy".equals(name)){
            hasDestroyMethod = true
            return LifecycleOnDestroyMethodVisitor(mv)
        }

        return mv
    }

    override fun visitEnd() {
        if(!hasDestroyMethod){
            injectFixLeakMethodOndestroy(cv,className,superName)
        }
        super.visitEnd()


    }
}