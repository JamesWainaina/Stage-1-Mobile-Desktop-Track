plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}


android {
    namespace = "com.example.dataencryptionproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.dataencryptionproject"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-analytics")
    // Google Sign-In for Firebase Authentication
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")
    // FirebaseUI for Authentication (Optional for easy implementation of social login)
    implementation("com.firebaseui:firebase-ui-auth:7.2.0")
    // Facebook SDK (if you want Facebook login)
    implementation("com.facebook.android:facebook-login:13.1.0")
}
