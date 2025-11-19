-keep class com.google.mediapipe.** { *; }
-keep class com.google.mediapipe.tasks.** { *; }
-keep class org.tensorflow.lite.** { *; }
-keep class com.google.protobuf.** { *; }
-keep class com.google.flogger.** { *; }
-keep class com.google.common.** { *; }
-keep class net.lateinit.noiseguard.data.ml.** { *; }
-keep class net.lateinit.noiseguard.domain.label.** { *; }
-keep class net.lateinit.noiseguard.presentation.viewmodel.HomeViewModel { *; }
-dontwarn com.google.mediapipe.**
-dontwarn org.tensorflow.lite.**
-dontwarn com.google.protobuf.**
-dontwarn com.google.flogger.**
-dontwarn com.google.common.**

# Firebase Analytics - AD_ID 관련 클래스 무시
-dontwarn com.google.android.gms.ads.identifier.**
-dontnote com.google.android.gms.ads.identifier.**

# 사용하지 않는 광고 관련 클래스는 제거해도 됨
-dontwarn com.google.android.gms.measurement.**
-dontwarn com.google.android.gms.ads.identifier.AdvertisingIdClient
-dontwarn com.google.android.gms.ads.identifier.AdvertisingIdClient$Info
