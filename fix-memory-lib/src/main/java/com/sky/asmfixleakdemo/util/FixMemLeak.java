package com.sky.asmfixleakdemo.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.RequiresApi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @author: xuzhiyong
 * @date: 2021/7/13  上午11:59
 * @Email: 18971269648@163.com
 * @description:
 */
public class FixMemLeak {
    private static Field field;
    private static boolean hasField = true;

    private static void fixViewLeak(Context context) {
        if (!hasField) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;

        }

        String[] arr = new String[]{"mLastSrvView"};
        for (String param : arr) {
            try {
                if (field == null) {
                    field = imm.getClass().getDeclaredField(param);
                }
                if (field == null) {
                    hasField = false;
                }
                if (field != null) {
                    field.setAccessible(true);
                    field.set(imm, null);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static void fixHWPhoneMemoryLeak(Activity activity){
        fixViewLeak(activity);
        fixHwChangeButtonWindowCtrl(activity);
    }

    private static void fixHwChangeButtonWindowCtrl(Activity activity){
        try {
            Class factoryImplClass = Class.forName("android.app.HwChangeButtonWindowCtrl");
            if(factoryImplClass != null){
                Field mInstanceMapField = factoryImplClass.getDeclaredField("mInstanceMap");
                Field mActivityField = factoryImplClass.getDeclaredField("mActivity");
                if(mInstanceMapField != null && mActivityField != null){
                    mInstanceMapField.setAccessible(true);
                    mActivityField.setAccessible(true);
                    HashMap<Integer,?> mInstanceMap = (HashMap<Integer, ?>) mInstanceMapField.get(null);
                    if(mInstanceMap != null){
                        Object object = mInstanceMap.get(Integer.valueOf(activity.hashCode()));
                        if(object != null){
                            mActivityField.set(object,null);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


       try {
           Class factoryImplClass = Class.forName("huawei.android.common.HwFrameworkFactoryImpl");
           if(factoryImplClass != null){
               Object hwFrameworkFactoryImpl =  factoryImplClass.newInstance();
               Method method = factoryImplClass.getDeclaredMethod("getHwChangeButtonWindowCtrl", Activity.class);
               if(method != null && hwFrameworkFactoryImpl != null){
                   Object hwChangeButtonWindowCtrl = method.invoke(hwFrameworkFactoryImpl,activity);
                   if(hwChangeButtonWindowCtrl != null){
                       Method destoryViewMethod = hwChangeButtonWindowCtrl.getClass().getDeclaredMethod("destoryView");
                       if(destoryViewMethod != null){
                           destoryViewMethod.invoke(hwChangeButtonWindowCtrl);
                       }
                   }
               }
           }

       }catch (Exception e){
           e.printStackTrace();
       }

        try {

            Class hwSBClass = Class.forName("com.huawei.hms.utils.HMSBIInitializer");
            if(hwSBClass != null){
                Field fieldC = hwSBClass.getDeclaredField("c");
                Field fieldD = hwSBClass.getDeclaredField("d");
                if(fieldC != null && fieldD != null){
                    fieldC.setAccessible(true);
                    fieldD.setAccessible(true);
                    Object instans = fieldC.get(null);
                    if(instans != null){
                        fieldD.set(instans,null);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
