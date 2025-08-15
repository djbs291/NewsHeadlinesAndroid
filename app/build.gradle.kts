plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
  id("org.jetbrains.kotlin.kapt")
//id("com.google.devtools.ksp")
}

android {
  namespace = "com.example.news"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.example.news"
    minSdk = 24
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"
    buildConfigField("String", "NEWS_API_KEY", "\"${project.findProperty("NEWS_API_KEY") ?: ""}\"")
    vectorDrawables { useSupportLibrary = true }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }

  buildFeatures {
    compose = true
    buildConfig = true // <-- fixes the BuildConfig error
  }

  // With Kotlin 2.x + compose plugin, DO NOT set composeOptions
  packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    isCoreLibraryDesugaringEnabled = true
  }
  kotlinOptions { jvmTarget = "17" }
}

kapt {
  correctErrorTypes = true
  includeCompileClasspath = true
}

dependencies {
  // Compose
  implementation(platform("androidx.compose:compose-bom:2025.01.00"))
  implementation("androidx.activity:activity-compose:1.9.3")
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-tooling-preview")
  debugImplementation("androidx.compose.ui:ui-tooling")
  implementation("androidx.compose.material3:material3:1.3.0")
  implementation("androidx.navigation:navigation-compose:2.8.3")

  // Material Components (provides Theme.Material3.* XML)
  implementation("com.google.android.material:material:1.12.0")

  // Images (Coil)
  implementation("io.coil-kt:coil-compose:2.7.0")

  // Network / JSON
  implementation("com.squareup.retrofit2:retrofit:2.11.0")
  implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
  implementation("com.squareup.moshi:moshi:1.15.1")
  kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")
  implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.14")

  // Desugaring for java.time with minSdk 24
  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

  // Tests
  testImplementation("junit:junit:4.13.2")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
