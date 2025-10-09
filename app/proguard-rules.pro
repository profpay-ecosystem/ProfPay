# Keep annotations
-keepattributes *Annotation*

#######################################
# Room
#######################################

# Сохраняем аннотированные Entity, Dao и Database классы
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Разрешаем доступ Room к аннотированным методам
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

-keep class com.profpay.wallet.data.database.AppDatabase { *; }

-dontwarn io.netty.internal.tcnative.AsyncSSLPrivateKeyMethod
-dontwarn io.netty.internal.tcnative.AsyncTask
-dontwarn io.netty.internal.tcnative.Buffer
-dontwarn io.netty.internal.tcnative.CertificateCallback
-dontwarn io.netty.internal.tcnative.CertificateCompressionAlgo
-dontwarn io.netty.internal.tcnative.CertificateVerifier
-dontwarn io.netty.internal.tcnative.Library
-dontwarn io.netty.internal.tcnative.SSL
-dontwarn io.netty.internal.tcnative.SSLContext
-dontwarn io.netty.internal.tcnative.SSLPrivateKeyMethod
-dontwarn io.netty.internal.tcnative.SSLSessionCache
-dontwarn io.netty.internal.tcnative.SessionTicketKey
-dontwarn io.netty.internal.tcnative.SniHostNameMatcher

-dontwarn org.apache.log4j.**
-dontwarn org.apache.logging.log4j.**
-dontwarn org.eclipse.jetty.alpn.**
-dontwarn org.eclipse.jetty.npn.**
-dontwarn reactor.blockhound.integration.BlockHoundIntegration

-dontwarn me.pushy.**
-keep class me.pushy.sdk.Pushy { *; }
-keep class me.pushy.sdk.services.** { *; }
-keep class me.pushy.sdk.util.exceptions.** { *; }
-keep class android.support.v4.app.** { *; }

# --- Sentry SDK ---
-keepattributes LineNumberTable,SourceFile
-keep class io.sentry.** { *; }
-dontwarn io.sentry.**

# --- Android SDK Sentry ---
-keep class io.sentry.android.** { *; }
-dontwarn io.sentry.android.**

# --- Sentry для OkHttp или Timber ---
-keep class io.sentry.okhttp.** { *; }
-keep class io.sentry.android.timber.** { *; }

# Netty epoll
-keep class io.netty.channel.epoll.Native { *; }
-keep class io.netty.channel.epoll.** { *; }
-dontwarn io.netty.channel.epoll.**

# Keep BouncyCastle security providers
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
-keep class X9.** { *; }

-keep class org.bouncycastle.jce.provider.BouncyCastleProvider { *; }
-keepclassmembers class * extends java.security.Provider {
    public <init>(...);
}

# Keep all gRPC generated classes (build/generated)
-keep class **.grpc.** { *; }

# Keep all proto messages
-keep class **.proto.** { *; }

# Keep ManagedChannel methods
-keepclassmembers class io.grpc.ManagedChannel* {
    public *;
}

# Prevent obfuscation of shutdown/shutdownNow methods
-keepclassmembers class * {
    public void shutdown(...);
    public void shutdownNow(...);
}

# Don't warn about gRPC internals
-dontwarn io.grpc.**
-dontwarn javax.annotation.**
-dontwarn com.google.protobuf.**