package com.sky.asmplugin

import com.android.build.gradle.AppExtension
import com.sky.asmplugin.config.Config
import com.sky.asmplugin.transform.FixMemoryLeakTransformer
import org.gradle.BuildAdapter
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle

class ASMPlugin : Plugin<Project>{

    override fun apply(project: Project) {
        System.out.println("ASM plugin start-------------------")

        project.gradle.addBuildListener(object : BuildAdapter() {

            override fun buildFinished(result: BuildResult) {
                println("build finish.........")
                Config.setNullManager()
            }
        })
        val android = project.extensions.getByType(AppExtension::class.java)
        android.registerTransform(FixMemoryLeakTransformer(project))
    }
}