package com.sky.asmplugin

import com.android.build.gradle.AppExtension
import com.sky.asmplugin.config.Config
import com.sky.asmplugin.transform.FixMemoryLeakTransformer
import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

class ASMPlugin : Plugin<Project>{

    override fun apply(project: Project) {
        System.out.println(">>> huawei-fix-memory plugin start-------------------")

        with(project) {
            val android = extensions.getByType(AppExtension::class.java)
            if(android != null){
                val hwFixExtension = extensions.create("hwfix", HWFixExtension::class.java)

                android.applicationVariants.all {

                    if(!hwFixExtension.enable){
                        showLogFirst("enable"){
                            println(">>> HWFixPlugin is not disabled.")
                        }
                        return@all
                    }

                    dependencies.add("implementation","io.github.xuzhiyong017:fix-memory-lib:${hwFixExtension.useLibVersion}")
                    showLogFirst("dependencieslib"){
                        println(">>> use fix-memory-lib version is ${hwFixExtension.useLibVersion}")
                    }

                }

                gradle.addBuildListener(object : BuildAdapter() {
                    override fun buildStarted(gradle: Gradle) {
                        isShowLogMap.clear()
                    }
                    override fun buildFinished(result: BuildResult) {
//                    println("build finish.........")
                        Config.setNullManager()
                    }
                })

                android.registerTransform(FixMemoryLeakTransformer(project))
            }else{
                println("need first apply plugin:'com.android.application'")
            }

        }
    }


    var isShowLogMap = mutableMapOf<String,Boolean>()

    fun showLogFirst(tag:String,run: ()-> Unit){
        if(!isShowLogMap.getOrDefault(tag,false)){
            isShowLogMap[tag] = true
            run.invoke()
        }
    }

}