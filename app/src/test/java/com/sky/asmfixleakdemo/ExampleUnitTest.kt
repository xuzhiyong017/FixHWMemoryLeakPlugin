package com.sky.asmfixleakdemo

import org.junit.Test

import org.junit.Assert.*
import java.lang.reflect.Field

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testString(){
        println("com/sky/asmfixleak/MainActivity".indexOf("MainActivity"))
    }

    @Test
    fun getJavaValueByReflect(){
        val test = com.sky.asmfixleakdemo.Test()
        val field:Field = test.javaClass.getDeclaredField("name")
        field.isAccessible = true
        println(field.get(test))
    }

    @Test
    fun testLoop(){
        for (i in 0 until 10){
            print(i)
        }
        println()
    }

    @Test
    fun testIn(){
        val list = mutableListOf<String>()
        list.add("MainActivity")
        list.add("MainActivity2")
        list.add("MainActivity3")
        list.add("MainActivity4")
        list.add("MainActivity5")

        val isIn = "MainActivity5" in list
        println(isIn)
    }
}