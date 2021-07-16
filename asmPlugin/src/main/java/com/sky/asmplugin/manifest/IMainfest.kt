package com.sky.asmplugin.manifest

/**
 * @author: xuzhiyong
 * @date: 2021/7/13  下午4:38
 * @Email: 18971269648@163.com
 * @description:
 */
interface IMainfest {

    fun getActivities():List<String>
    fun getPackageName():String
}