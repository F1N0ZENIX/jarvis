plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android { namespace = "com.f1n0zenix.jarvis"; compileSdk = 35
 defaultConfig { applicationId = "com.f1n0zenix.jarvis"; minSdk = 26; targetSdk = 35; versionCode = 12; versionName = "0.9.3" }
 signingConfigs {
  create("jarvis") {
   val path=System.getenv("JARVIS_KEYSTORE_PATH")
   if(!path.isNullOrBlank()) {
    storeFile=file(path)
    storePassword=System.getenv("JARVIS_KEYSTORE_PASSWORD")
    keyAlias=System.getenv("JARVIS_KEY_ALIAS")
    keyPassword=System.getenv("JARVIS_KEY_PASSWORD")
   }
  }
 }
 buildTypes {
  getByName("debug") {
   if(!System.getenv("JARVIS_KEYSTORE_PATH").isNullOrBlank()) signingConfig=signingConfigs.getByName("jarvis")
  }
 }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
dependencies { implementation("androidx.core:core:1.15.0") }