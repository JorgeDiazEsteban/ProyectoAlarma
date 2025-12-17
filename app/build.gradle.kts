plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.proyecto_alarma_pm"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.proyecto_alarma_pm"
        minSdk = 24
        targetSdk = 36
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
    buildFeatures{
        viewBinding = true
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
    dependencies {
        //It allows you to connect camera x to the phone's camera better than camera 2.
        implementation("androidx.camera:camera-camera2:1.3.0")
        //It allows the camera to know when your app is open, paused, or closed. It prevents the camera from staying on.
        implementation("androidx.camera:camera-lifecycle:1.3.0")
        //It gives you access to the PreviewView. It's the visual component you'll put into the XML so the user can see what the camera is pointing at in real time.
        implementation("androidx.camera:camera-view:1.3.0")
        // ML Kit for text recognition
        //It uses Google´s libraries and allows to convert the image to a String
        implementation("com.google.mlkit:text-recognition:16.0.0")
    }
}