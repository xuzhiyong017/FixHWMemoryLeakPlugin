package com.sky.asmplugin.manifest

import com.android.build.gradle.tasks.ManifestProcessorTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import java.io.File
import java.lang.reflect.Field
import java.util.regex.Pattern

/**
 * @author: xuzhiyong
 * @date: 2021/7/13  下午4:40
 * @Email: 18971269648@163.com
 * @description:
 */
class ManifestManager {
    var sManifestIml:IMainfest? = null
    var currentDir:String? = null

   fun getActivities(project: Project,variantDir:String): List<String> {
        if(sManifestIml == null || !variantDir.equals(currentDir)){
            sManifestIml = ManifestIml(manifestPath(project,variantDir))
            currentDir = variantDir
        }
        return sManifestIml!!.getActivities()
    }

    fun getPackageName():String{
        if(sManifestIml == null){
            throw GradleException("please execute getActivities method ...")
        }
        return sManifestIml!!.getPackageName()
    }

    private fun manifestPath(project: Project, variantDir: String): String {
        val variantDirArray = variantDir.split(Pattern.quote(File.separator))
        var variantName = ""

        variantDirArray.forEach {
            variantName += it.capitalize()
        }
        println(">>> variantName:${variantName}")

        val processManifestTask: Task = project.tasks.getByName("process${variantName}Manifest")

        if(processManifestTask != null){
            var result:File? = null
            var manifestOutputFile:File? = null
            var instantRunManifestOutputFile:File? = null

            try{
                val manifestOutputFileField:Field = processManifestTask.javaClass.getDeclaredField("manifestOutputFile")
                val instantRunManifestOutputFileField:Field = processManifestTask.javaClass.getDeclaredField("instantRunManifestOutputFile")

                if(manifestOutputFileField != null && instantRunManifestOutputFileField != null){
                    manifestOutputFileField.isAccessible = true
                    instantRunManifestOutputFileField.isAccessible = true
                    manifestOutputFile = manifestOutputFileField.get(processManifestTask) as File
                    instantRunManifestOutputFile = instantRunManifestOutputFileField.get(processManifestTask) as File
                }
            }catch (e:Exception){
                var clazz:Class<*> = processManifestTask.javaClass
                while (clazz != null && clazz != ManifestProcessorTask::class.java){
                    clazz = clazz.superclass
                }

                if(clazz == ManifestProcessorTask::class.java){
                    val dirField:Field = clazz.getDeclaredField("manifestOutputDirectory")
                    if(dirField != null){
                        dirField.isAccessible = true
                       var dir = dirField.get(processManifestTask)
                        if(dir is File){
                            manifestOutputFile = File(dir,"AndroidManifest.xml")
                        }else if(dir is String){
                            manifestOutputFile = File(dir,"AndroidManifest.xml")
                        }else if(dir is Provider<*>){
                            manifestOutputFile = File((dir as Provider<Directory>).get().asFile,"AndroidManifest.xml")
                        }
                    }

                    val instantDirField:Field = clazz.getDeclaredField("instantRunManifestOutputDirectory")
                    if(instantDirField != null){
                        instantDirField.isAccessible = true
                        var dir = instantDirField.get(processManifestTask)
                        if(dir is File){
                            instantRunManifestOutputFile = File(dir,"AndroidManifest.xml")
                        }else if(dir is String){
                            instantRunManifestOutputFile = File(dir,"AndroidManifest.xml")
                        }else if(dir is Provider<*>){
                            instantRunManifestOutputFile = File((dir as Provider<Directory>).get().asFile,"AndroidManifest.xml")
                        }
                    }

                    if(manifestOutputFile == null && instantRunManifestOutputFile == null){
                        throw GradleException("can't get manifest file")
                    }

                    //打印
//                    println(">>> manifestOutputFile:${manifestOutputFile} ${manifestOutputFile?.exists()}")
//                    println(">>> instantRunManifestOutputFile:${instantRunManifestOutputFile} ${instantRunManifestOutputFile?.exists()}")

                    result = manifestOutputFile

                    try {
                        val instantRunTask = project.tasks.getByName("transformClassesWithInstantRunFor${variantName}")
                        //查找instant run是否存在且文件存在
                        if (instantRunTask != null && instantRunManifestOutputFile?.exists() == true) {
                            println("Instant run is enabled and the manifest is exist.")
                            if (manifestOutputFile?.exists() == false) {
                                //因为这里只是为了读取activity，所以无论用哪个manifest差别不大
                                //正常情况下不建议用instant run的manifest，除非正常的manifest不存在
                                //只有当正常的manifest不存在时，才会去使用instant run产生的manifest
                                result = instantRunManifestOutputFile
                            }
                        }
                    }catch (e:Exception){

                    }

                    //最后检测文件是否存在，打印
                    if (result?.exists() == false) {
                        println (">>> AndroidManifest.xml not exist")
                    }

//                    println(">>> AndroidManifest.xml 路径：$result")

                    return result!!.absolutePath
                }else{
                    throw GradleException("not found manifest file path")
                }

                println(">>> manifestOutputFile---- ${manifestOutputFile} ")
            }

        }
        return ""
    }

    fun getDeclaredField(`object`: Any, fieldName: String?): Field? {
        var field: Field? = null
        var clazz: Class<*> = `object`.javaClass
        while (clazz != ManifestProcessorTask::class.java) {
            try {
                field = clazz.getDeclaredField(fieldName)
                return field
            } catch (e: java.lang.Exception) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了
            }
            clazz = clazz.superclass
        }
        return null
    }
}