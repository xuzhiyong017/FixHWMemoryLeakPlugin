package com.sky.asmfixleak;

import com.sky.asmfixleak.util.FixMemLeak;

/**
 * @author: xuzhiyong
 * @date: 2021/7/15  上午10:57
 * @Email: 18971269648@163.com
 * @description:
 */
public class TestActivity extends MainActivity2{


    @Override
    protected void onDestroy() {
        super.onDestroy();
        FixMemLeak.fixHWPhoneMemoryLeak(this);
    }
}
