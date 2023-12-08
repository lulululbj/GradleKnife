# GradleKnife
Gradle Plugin 练手项目，基于 `gradle 8.1.4` && `kotlin 1.9.0` 。

`knife-plugin` 为插件目录，`inlcudeBuild` 到主 module。

`knife-plugin` 目录下单个 module 为一个独立插件。

## methodTrace

- 获取方法耗时
- 获取方法参数类型以及参数值，返回值类型以及返回值

Asm 事件模型实现：[MethodTraceClassVisitor](https://github.com/lulululbj/GradleKnife/blob/main/knife-plugin/methodTrace/src/main/java/luyao/plugin/methodTrace/MethodTraceClassVisitor.kt)

Asm Tree Api 实现： [MethodTraceClassNode](https://github.com/lulululbj/GradleKnife/blob/main/knife-plugin/methodTrace/src/main/java/luyao/plugin/methodTrace/MethodTraceClassNode.kt)

## 参考资料

1. [pluginDemo](https://github.com/MRwangqi/pluginDemo)
2. [Android-ASM](https://github.com/RicardoJiang/Android-ASM)