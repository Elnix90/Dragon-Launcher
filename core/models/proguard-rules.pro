-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep enum * { *; }
-keep class kotlin.reflect.** { *; }
