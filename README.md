# FixHWMemoryLeakPlugin
使用ASM+Gradle插件解决华为和荣耀手机导致的内存泄漏的问题
# 特性
* plugin使用kotlin语言编写
* 支持增量编译
* 持并发处理class
* 默认处理所有的Activity
* 支持处理第三方库
* 使用ASM新增修改方法
* 处理合并后的Manifest的文件

# 开发环境
 * kotlin语言
 * gradle插件 3.4.2
 * gradle库 5.4.1
 * ASM 9.2

# 使用方式
  在项目的bulid.gradle 种加入
```

 classpath 'io.github.xuzhiyong017:huawei-memoryleak-plugin:x.y.z'

```
 然后在app module 的build.gradle中
 使用
 ```
 apply plugin: ‘com.android.application‘
 apply plugin: ‘com.memoryleak.plugin‘
 ```
 或者
 ```
 plugins{
    id 'com.android.application'
    id 'com.memoryleak.plugin'
}
 ```
 这样项目就会在编译的过程中自动去处理class了，在项目所有页面的onDestroy中添加处理代码了
 
 
 
