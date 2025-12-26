import java.util.Properties

val localPropsFile = rootProject.file("local.properties")
val localProps = Properties()
if (localPropsFile.exists()) {
    localProps.load(localPropsFile.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.deuna_integration"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.deuna_integration"
        minSdk = 35
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15" // or the version matching your BOM
    }

    defaultConfig {
        buildConfigField(
            "String",
            "DEUNA_DOMAIN",
            "\"${localProps["DEUNA_DOMAIN"] ?: ""}\""
        )
        buildConfigField(
            "String",
            "DEUNA_PUBLIC_API_KEY",
            "\"${localProps["DEUNA_PUBLIC_API_KEY"] ?: ""}\""
        )
        buildConfigField(
            "String",
            "DEUNA_USER_TOKEN",
            "\"${localProps["DEUNA_USER_TOKEN"] ?: ""}\""
        )
        buildConfigField(
            "String",
            "DEUNA_ORDER_TOKEN",
            "\"${localProps["DEUNA_ORDER_TOKEN"] ?: ""}\""
        )
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
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.github.deuna-developers:deuna-sdk-android:2.9.14")

    val composeBom = platform("androidx.compose:compose-bom:2025.01.00") // use a stable version

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
}

android.buildFeatures.buildConfig = true