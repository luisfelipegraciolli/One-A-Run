# Preservar anotações e nomes de campos SerializedName do Gson
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Preservar classes de modelo/DTO usadas com Gson e MQTT
-keep class com.shiwa.onearun.data.remote.api.** { *; }
-keep class com.shiwa.onearun.data.location.LocationUpdate { *; }
-keep class com.shiwa.onearun.race.MqttProgressDto { *; }
-keep class com.shiwa.onearun.race.MqttResultDto { *; }
-keep class com.shiwa.onearun.race.ProgressPoint { *; }

# Preservar dependências do HiveMQ MQTT Client e Netty
-keep class io.netty.** { *; }
-keepclassmembers class io.netty.** { *; }
-keep class com.hivemq.client.** { *; }
-keepclassmembers class com.hivemq.client.** { *; }
-keepclassmembers class org.jctools.** { *; }

# Suppress warnings for optional/missing dependencies used by Netty, HiveMQ, and other libraries
-dontwarn com.aayushatharva.brotli4j.**
-dontwarn com.github.luben.zstd.**
-dontwarn com.google.protobuf.**
-dontwarn com.jcraft.jzlib.**
-dontwarn com.ning.compress.**
-dontwarn com.oracle.svm.core.annotate.**
-dontwarn io.netty.channel.epoll.**
-dontwarn io.netty.handler.codec.http.**
-dontwarn io.netty.handler.proxy.**
-dontwarn io.netty.internal.tcnative.**
-dontwarn lzma.sdk.**
-dontwarn net.jpountz.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.logging.log4j.**
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.eclipse.jetty.**
-dontwarn org.jboss.marshalling.**
-dontwarn org.osgi.annotation.**
-dontwarn org.slf4j.**
-dontwarn reactor.blockhound.**
