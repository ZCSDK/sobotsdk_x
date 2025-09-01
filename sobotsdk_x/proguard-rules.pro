#-------------------------------------------基本配置-------------------------------------------
# 代码混淆压缩比，在0~7之间，默认为5，一般不做修改
-optimizationpasses 5
# 混淆后类名都小写
-dontusemixedcaseclassnames
# 不跳过非公共的库的类成员
-dontskipnonpubliclibraryclasses
# 不做预校验
-dontpreverify
# 屏蔽警告
-ignorewarnings
# 保留注解
-keepattributes *Annotation*
# 保留泛型
-keepattributes Signature
# 记录生成的日志数据
-verbose
# 优化时允许访问并修改有修饰符的类和类的成员
-allowaccessmodification
# 不进行优化
-dontoptimize
# 不进行预检
-dontpreverify


#-------------------------------------------Android系统组件保护-------------------------------------------
# 保持Activity、Fragment等组件不被混淆
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# 保持自定义View不被混淆
-keepclassmembers class * extends android.view.View {
    void set*(***);
    *** get*();
}

# 保持Parcelable不被混淆
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保持R文件不被混淆
-keepclassmembers class **.R$* {
    public static <fields>;
}
-keep class **.R

# 保持Serializable不被混淆
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

#-------------------------------------------反射和注解保护-------------------------------------------
# 保持注解相关
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

# 保持native方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保持枚举enum类不被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保持自定义控件不被混淆
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

#-------------------------------------------资源文件保护-------------------------------------------
# 保持资源文件访问
-keepclassmembers class **.R$* {
    public static <fields>;
}

# 保持WebView相关
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}

# 保持JS接口
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void *(android.webkit.WebView, java.lang.String);
}



#sobot SDK
-keep class com.sobot.chat.** {*;}
-dontwarn com.sobot.chat.**


# OkHttp3
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

## ----------------------------------
##      Glide相关
## ----------------------------------
-keep class com.bumptech.glide.Glide { *; }
-keep class com.bumptech.glide.request.RequestOptions {*;}
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-dontwarn com.bumptech.glide.**

## ----------------------------------
##      Picasso相关
## ----------------------------------
-keep class com.squareup.picasso.Picasso { *; }
-dontwarn com.squareup.okhttp.**
-dontwarn com.squareup.picasso.**

## ----------------------------------
##      Fresco相关
## ----------------------------------
-keep class com.facebook.fresco.** { *; }
-keep class com.facebook.imagepipeline.** { *; }

## ----------------------------------
##      UIL相关
## ----------------------------------
-keep class com.nostra13.universalimageloader.** { *; }
-keepclassmembers class com.nostra13.universalimageloader.** {*;}
-dontwarn com.nostra13.universalimageloader.**

# 保留所有 TypeToken 子类不被混淆，防止泛型信息丢失
-keep class com.sobot.chat.gson.reflect.TypeToken {
    *;
}
# 保留泛型签名信息
-keepattributes Signature
# 保留 Gson 序列化类的字段名和类型
-keepclassmembers class * {
    @com.sobot.chat.gson.annotations.SerializedName <fields>;
}


