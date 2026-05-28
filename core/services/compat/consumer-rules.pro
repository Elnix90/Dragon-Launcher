# Do not mignify any Workspace-related stuff dur to boot crashes
-keep class org.elnix.dragonlauncher.common.serializables.WorkspaceState { *; }
-keep class org.elnix.dragonlauncher.common.serializables.Workspace { *; }
-keep class org.elnix.dragonlauncher.common.serializables.AppOverride { *; }

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep enum * { *; }


# Gson essentials
-keepattributes Signature


# Keep adapters
-keep class org.elnix.dragonlauncher.common.serializables.SwipeActionAdapter { *; }
