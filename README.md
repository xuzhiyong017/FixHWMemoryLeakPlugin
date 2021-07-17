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
 
# 项目心得
 * 熟悉了使用kotlin 开发Android Gradle Plugin 的流程及具体实现
 * 熟悉了使用maven插件和maven-publish插件发布jar包和aar包
 * 熟悉了maven Central中央仓库发布包的流程
 * 熟悉了ASM字节码插桩在方法修改及增加的过程中的处理
 * 在开发插件过程中遇到的一些问题通过阅读官方文档解决
 * 开发过程中还是有很多细节优化点

# License
```
Copyright 2013 xuzhiyong017

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 
```
