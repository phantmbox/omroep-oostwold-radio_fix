plugins { id("com.android.application") }
android {
 namespace = "nl.omroepoostwold.carradio"
 compileSdk = 35
 defaultConfig {
  applicationId = "nl.omroepoostwold.carradio"
  minSdk = 23
  targetSdk = 28
  versionCode = 2
  versionName = "1.1"
 }
}
dependencies {
 implementation("androidx.media3:media3-exoplayer:1.5.1")
 implementation("androidx.media3:media3-exoplayer-hls:1.5.1")
}
