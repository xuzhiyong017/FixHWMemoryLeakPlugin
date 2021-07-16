package com.sky.asmplugin.manifest

import groovy.util.XmlSlurper
import groovy.util.slurpersupport.GPathResult
import org.w3c.dom.Document
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author: xuzhiyong
 * @date: 2021/7/13  下午4:44
 * @Email: 18971269648@163.com
 * @description:
 */
class ManifestIml : IMainfest{

    lateinit var filePath:String
    var manifest: Document? = null
    var mPackageName: String? = null
    var activitys = mutableListOf<String>()

    constructor(path:String){
        filePath = path
    }

    override fun getActivities(): List<String> {
        init()
        return activitys
    }

    override fun getPackageName(): String {
        init()
        return mPackageName ?:""
    }

    fun init(){
        if(filePath.isNullOrEmpty()) return

        if(manifest == null){
            val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            val db: DocumentBuilder = dbf.newDocumentBuilder()
            val doc: Document = db.parse(filePath)
            manifest = doc

            println(">>> start Parse AndroidManifest.xml")
            val manifest = doc.documentElement
            mPackageName = manifest.getAttribute("package")
            val nodelist = manifest.getElementsByTagName("activity")
            for (index in 0 until nodelist.length){
                val element = nodelist.item(index)
                val attrs = element.attributes
                for (i in 0 until attrs.length){
                    if("android:name".equals(attrs.item(i).nodeName)){
                        var activityName = attrs.item(i).nodeValue
                        if(activityName.startsWith(".")){
                            activityName = mPackageName + activityName
                        }
                        activitys.add(activityName)
                    }
                }
            }
      }
    }
}