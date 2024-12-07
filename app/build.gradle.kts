import org.jetbrains.kotlin.codegen.intrinsics.ArrayOf

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.wire)
}

android {
    namespace = "com.wemaka.weatherapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wemaka.weatherapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        resourceConfigurations.addAll(arrayOf("en", "ru"))

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }
}

wire {
    java {}
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.gridlayout)

    implementation(libs.mpandroidChart)
    implementation(libs.roundCornerprogressbar)
    implementation(libs.legacy.support.v4)
    implementation(libs.okhttp)
    implementation(libs.open.meteo)
    implementation(libs.play.services.location)
    implementation(libs.swiperefreshlayout)
    implementation(libs.fragment)

    implementation(libs.lombok)
    implementation(libs.preference)
    annotationProcessor(libs.lombok)

    implementation(libs.datastore.core)
    implementation(libs.datastore.rxjava3)
    implementation(libs.rxandroid)

    implementation(libs.blurview)
    implementation(libs.locale.helper.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}