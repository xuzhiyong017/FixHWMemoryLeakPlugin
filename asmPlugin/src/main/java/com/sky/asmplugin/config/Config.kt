package com.sky.asmplugin.config

import com.sky.asmplugin.manifest.ManifestManager
import org.gradle.api.Project

/**
 * @author: xuzhiyong
 * @date: 2021/7/14  上午11:04
 * @Email: 18971269648@163.com
 * @description:
 */
object Config {

    var activitys = listOf<String>()
    var activityListSimpleName = mutableListOf<String>()
    var packageName = ""
    var oldVariantDir = ""
    private var manager:ManifestManager? = null

    @Synchronized
    fun setNullManager(){
        manager = null
    }

    @Synchronized
    fun initManifest(project: Project, variantDir: String){
        if(manager == null || !oldVariantDir.equals(variantDir)){
            oldVariantDir = variantDir
            manager = ManifestManager()
            activitys = manager!!.getActivities(project, variantDir)
            activityListSimpleName.clear()
            activitys.forEach {
                activityListSimpleName.add(it.substring(it.lastIndexOf(".")+1))
            }
            packageName = manager!!.getPackageName()
        }
    }

    fun printActivitysInfo() {
//       println(">>> printActivityInfo activitys size ${activitys.size}  ${this} ${this.manager}")
//        activityListSimpleName.forEach {
//            println(">>> printActivityInfo activityListSimpleName size ${it}")
//        }
    }
}