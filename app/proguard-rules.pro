-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService


-keepclasseswithmembernames class * {
native <methods>;
}
-keepclasseswithmembers class * {
public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers enum * {
public static **[] values();
public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-dontwarn com.sina.**
-keep class com.sina.** { *;}

-dontwarn com.handmark.**
-keep class com.handmark.** { *;}

-dontwarn com.viewpagerindicator.**
-keep class com.viewpagerindicator.** { *;}

-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }

-keepattributes Signature
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.examples.android.model.** { *; }

-dontwarn com.ant.**
-keep class com.ant.** { *; }

-dontwarn com.nineoldandroids.**
-keep class com.nineoldandroids.** { *; }

-dontwarn m.framework.**
-keep class m.framework.** { *; }

-dontwarn org.apache.http.entity.mime.**
-keep class org.apache.http.entity.mime.** { *; }

-dontwarn com.handmark.pulltorefresh.library.**
-keep class com.handmark.pulltorefresh.library.** { *; }

-dontwarn com.loopj.android.http.**
-keep class com.loopj.android.http.** { *; }

-dontwarn org.apache.http.**
-keep class org.apache.http.** { *; }

-dontwarn org.apache.http.**
-keep class org.apache.http.** { *; }

-dontwarn com.nostra13.universalimageloader.**
-keep class com.nostra13.universalimageloader.** { *; }

#-libraryjars   libs/android-support-v4.jar
-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment

-keep class com.google.gson.JsonObject { *; }
-keep class com.badlogic.** { *; }
-keep class * implements com.badlogic.gdx.utils.Json*
-keep class com.google.** { *; }

-keep class com.google.** {
    <fields>;
    <methods>;
}

-keepclassmembers class * extends java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Gson specific classes
-keep class sun.misc.Unsafe {
    <fields>;
    <methods>;
}

# -keep class com.google.gson.stream.** { *; }
# Application classes that will be serialized/deserialized over Gson
-keep class com.**.** {
    <fields>;
    <methods>;
}

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

-keep public class com.**.**.R$*{
public static final int *;
}

-keep public class wl.smartled.beans.DeviceBean {
    <methods>;
}


-dontwarn u.aly.**
-keep class u.aly.** { *; }