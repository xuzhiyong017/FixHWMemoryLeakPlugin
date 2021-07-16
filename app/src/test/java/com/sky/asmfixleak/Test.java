package com.sky.asmfixleak;

import java.lang.reflect.Field;

/**
 * @author: xuzhiyong
 * @date: 2021/7/14  下午5:10
 * @Email: 18971269648@163.com
 * @description:
 */
public class Test extends Flut{
    private int age = 10;

    public static void main(String[] args) {
        Test test = new Test();

        Class clasz = test.getClass();
        clasz = clasz.getSuperclass();

    }

    public static Field getDeclaredField(Object object, String fieldName){
        Field field = null ;

        Class<?> clazz = object.getClass() ;

        for(; clazz != Object.class ; clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(fieldName) ;
                return field ;
            } catch (Exception e) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了

            }
        }

        return null;
    }
}

