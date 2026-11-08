# Ktor 的 IntellijIdeaDebugDetector 引用 java.lang.management（桌面 JVM API，Android 没有）。
# 那段代码包在 try/catch(Throwable) 里，运行时安全，R8 只是不认这个类。
-dontwarn java.lang.management.**

# Keep annotations
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# skill 工具的入参类是 @Serializable 数据类：Koog 靠 serializer() 的 descriptor 生成
# input_schema，字段名就是模型看到的参数名。kotlinx-serialization 的 consumer 规则
# 已经保留 Companion/serializer()/descriptor，这里不用再加 keep。

# 不要加 -keep class androidx.compose.** { *; }
# Compose 各 AAR 自带 consumer 规则（proguard.txt），AGP 自动应用。
# 全量 keep 会让 R8 完全无法收缩：曾保留 5.3 万个类、154 万个成员，
# R8 单任务耗时 9m36s，APK 10.4MB。去掉后由 R8 只保留实际用到的 95 个图标。
