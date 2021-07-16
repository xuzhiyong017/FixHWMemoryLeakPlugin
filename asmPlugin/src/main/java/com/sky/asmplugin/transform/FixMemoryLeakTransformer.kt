package com.sky.asmplugin.transform

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.IntermediateFolderUtils
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import com.sky.asmplugin.config.Config
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Field
import java.util.concurrent.Callable
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry


/**
 * @author: xuzhiyong
 * @date: 2021/7/12  下午3:17
 * @Email: 18971269648@163.com
 * @description:
 */
class FixMemoryLeakTransformer(val project: Project) : Transform() {

    override fun getName(): String {
        return "__FixMemoryLeak__"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return true
    }

    override fun transform(transformInvocation: TransformInvocation?) {
        transformInvocation?.run {

              var rootLocation:File? = null
              try {
                  val rootPathField:Field = outputProvider.javaClass.getDeclaredField("rootLocation")
                  if(rootPathField != null){
                      rootPathField.isAccessible = true
                      rootLocation = rootPathField.get(outputProvider) as File
                  }

              } catch (e:Throwable) {
                  //android gradle plugin 3.0.0+ 修改了私有变量，将其移动到了IntermediateFolderUtils中去
                  val folderUtilsField:Field? = outputProvider.javaClass.getDeclaredField("folderUtils")
                  if(folderUtilsField != null){
                      folderUtilsField.isAccessible = true
                      rootLocation = (folderUtilsField.get(outputProvider) as IntermediateFolderUtils).rootFolder
                  }

              }catch (e:Exception){
                  val folderUtilsField:Field? = outputProvider.javaClass.getDeclaredField("folderUtils")
                  if(folderUtilsField != null){
                      folderUtilsField.isAccessible = true
                      rootLocation = (folderUtilsField.get(outputProvider) as IntermediateFolderUtils).rootFolder
                  }
              }
              if (rootLocation == null) {
                  throw GradleException("can't get transform root location")
              }
            var variantDir = rootLocation.absolutePath.split(getName() + File.separator)[1]
//            println(">>> ThreadName=${Thread.currentThread().name} rootLocation: ${rootLocation.absolutePath} variantDir ${variantDir}")

            if(!isIncremental){
                outputProvider.deleteAll()
            }
            Config.initManifest(project,variantDir)
            val workerExecutor:WaitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()

            inputs.forEach {
                it.jarInputs.forEach { jarInput ->
                    workerExecutor.execute(Callable<Any>{
                        processJarInput(jarInput,outputProvider,isIncremental)
                        return@Callable null
                    })
                }
                it.directoryInputs.forEach { directoryInput ->
                    workerExecutor.execute(Callable<Any> {
                        processDirectoryInput(directoryInput,outputProvider,isIncremental)
                        return@Callable null
                    })
                }
            }

            workerExecutor.waitForTasksWithQuickFail<Any>(true)
        }
    }

    private fun processDirectoryInput(
        directoryInput: DirectoryInput,
        outputProvider: TransformOutputProvider,
        incremental: Boolean
    ) {
        val dest = outputProvider.getContentLocation(directoryInput.name,directoryInput.contentTypes,directoryInput.scopes,Format.DIRECTORY)
        FileUtils.forceMkdir(dest)

        if(incremental){
            Config.printActivitysInfo()
            val srcDirPath = directoryInput.file.absolutePath
            val destDirPath = dest.absolutePath
            val fileStatusMap = directoryInput.changedFiles
            fileStatusMap.forEach {
                val inputFile = it.key
                val destFilePath = inputFile.absolutePath.replace(srcDirPath,destDirPath)
                val destFile = File(destFilePath)
                when(it.value){
                    Status.NOTCHANGED ->{}
                    Status.ADDED,Status.CHANGED ->{
                        FileUtils.touch(destFile)
                        transformSingleFile(inputFile,destFile)
                    }
                    Status.REMOVED ->{
                        if(destFile.exists()){
                            FileUtils.forceDelete(destFile)
                        }
                    }
                }
            }

        }else{
            transformDirectory(directoryInput.file,dest)
        }
    }

    private fun transformSingleFile(inputFile: File, destFile: File) {
        Config.printActivitysInfo()
        val string = inputFile.name.substring(0,inputFile.name.indexOf(".class"))
        if(string in Config.activityListSimpleName){
            println (">>>----------- deal with class file ${inputFile.name} -----------")
            val classReader = ClassReader(inputFile.readBytes())
            val classWriter = ClassWriter(classReader,ClassWriter.COMPUTE_MAXS)
            val classVisitor = FixLeakClassVisitor(classWriter)
            classReader.accept(classVisitor,ClassReader.EXPAND_FRAMES)
            val newByteCode = classWriter.toByteArray()
            val fos = FileOutputStream(destFile)
            fos.write(newByteCode)
            fos.close()
        }else{
            FileUtils.copyFile(inputFile, destFile)
        }
    }


    private fun transformDirectory(directoryInputFile:File, dest:File) {

        if(directoryInputFile.isDirectory){
            directoryInputFile.walk()
                .filter { it.isFile }
                .filter { it.extension in listOf("class")
                        && !it.name.startsWith("R\$")
                        && !"R.class".equals(it.name)
                        && !"BuildConfig.class".equals(it.name)
                }
                .forEach {
                    transformUseAsm(it)
            }
        }
        FileUtils.copyDirectory(directoryInputFile, dest)
    }

    private fun transformUseAsm(oldFile: File) {
        Config.printActivitysInfo()
        val string = oldFile.name.substring(0,oldFile.name.indexOf(".class"))
        if(string in Config.activityListSimpleName){
            println (">>>----------- deal with class file ${oldFile.name} -----------")
            val classReader = ClassReader(oldFile.readBytes())
            val classWriter = ClassWriter(classReader,ClassWriter.COMPUTE_MAXS)
            val classVisitor = FixLeakClassVisitor(classWriter)
            classReader.accept(classVisitor,ClassReader.EXPAND_FRAMES)
            val newByteCode = classWriter.toByteArray()
            val fos = FileOutputStream(oldFile.parentFile.absolutePath + File.separator + oldFile.name)
            fos.write(newByteCode)
            fos.close()
        }

    }

    private fun processJarInput(
        jarInput: JarInput,
        outputProvider: TransformOutputProvider,
        incremental: Boolean
    ) {

        val dest = outputProvider.getContentLocation(jarInput.file.absolutePath,jarInput.contentTypes,jarInput.scopes,Format.JAR)

        if(incremental){
            when(jarInput.status){
                Status.NOTCHANGED ->{}
                Status.ADDED,Status.CHANGED ->{
                    transformJar(jarInput,outputProvider)
                }
                Status.REMOVED ->{
                    if(dest.exists()){
                        FileUtils.forceDelete(dest)
                    }
                }
            }
        }else{
            transformJar(jarInput,outputProvider)
        }

    }

    private fun transformJar(jarInputFile: JarInput, outputProvider: TransformOutputProvider) {
        //将修改过的字节码copy到dest,就可以实现编译期间干预字节码的目的
        if(jarInputFile.file.absolutePath.endsWith(".jar")){
            var jarName = jarInputFile.name
            val md5 = DigestUtils.md5Hex(jarInputFile.file.absolutePath)
            if(jarName.endsWith(".jar")){
                jarName = jarName.substring(0,jarName.length - 4)
            }

            val jarFile = JarFile(jarInputFile.file)
            val enumeration = jarFile.entries()
            val tempFile = File(jarInputFile.file.parent + File.separator + "classes_temp.jar")
            if(tempFile.exists()){
                tempFile.delete()
            }

            val dest = outputProvider.getContentLocation(jarName + md5,jarInputFile.contentTypes,jarInputFile.scopes,Format.JAR)

            val jarOutputStream = JarOutputStream(FileOutputStream(tempFile))
            while (enumeration.hasMoreElements()){
                val jarEntry = enumeration.nextElement()
                val entryName = jarEntry.name
                val zipEntry = ZipEntry(entryName)
                val inputStream = jarFile.getInputStream(zipEntry)
                //deal class
                if(isNeedEditClass(entryName)){
                    println (">>>----------- deal with class file ${entryName} dest=${dest}----------")
                    jarOutputStream.putNextEntry(zipEntry)
                    val classReader = ClassReader(IOUtils.toByteArray(inputStream))
                    val classWriter = ClassWriter(classReader,ClassWriter.COMPUTE_MAXS)
                    val classVisitor = FixLeakClassVisitor(classWriter)
                    classReader.accept(classVisitor,ClassReader.EXPAND_FRAMES)
                    val newByteCode = classWriter.toByteArray()
                    jarOutputStream.write(newByteCode)
                }else{
                    jarOutputStream.putNextEntry(zipEntry)
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            jarFile.close()
            FileUtils.copyFile(tempFile, dest)
            tempFile.delete()
        }else{
            val dest = outputProvider.getContentLocation(jarInputFile.file.absolutePath,jarInputFile.contentTypes,jarInputFile.scopes,Format.JAR)
            FileUtils.copyFile(jarInputFile.file, dest)
        }
    }

    private fun isNeedEditClass(entryName: String): Boolean {
        if(entryName.endsWith(".class")){
            val realClassName = entryName.substring(entryName.lastIndexOf("/") + 1,entryName.lastIndexOf(".class"))
            if(realClassName in Config.activityListSimpleName){
                return true
            }
        }
        return false
    }
}