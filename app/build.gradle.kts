import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Nomor build dari GitHub Actions dipakai sebagai versionCode, supaya tiap APK punya nomor berbeda.
val nomorBuild: Int = providers.environmentVariable("GITHUB_RUN_NUMBER").orNull?.toIntOrNull() ?: 1
val akhiranVersi: String = if (providers.environmentVariable("CI").isPresent) "-b$nomorBuild" else "-lokal"

android {
    namespace = "io.github.serfusion.gasukom"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.serfusion.gasukom"
        minSdk = 24
        targetSdk = 36
        versionCode = nomorBuild
        versionName = "0.1.0$akhiranVersi"

        // HP warga hampir semuanya ARM. Membatasi ABI membuat APK jauh lebih kecil
        // karena pustaka native ML Kit untuk x86 tidak ikut.
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    // Kunci rilis diambil dari variabel lingkungan (secret GitHub), tidak pernah disimpan di repo.
    // Kalau belum diatur, APK rilis ditandatangani dengan kunci debug (cukup untuk uji coba).
    val berkasKunci = providers.environmentVariable("GASUKOM_KEYSTORE_FILE").orNull
    signingConfigs {
        if (berkasKunci != null) {
            create("rilis") {
                storeFile = file(berkasKunci)
                storePassword = providers.environmentVariable("GASUKOM_KEYSTORE_PASSWORD").orNull
                keyAlias = providers.environmentVariable("GASUKOM_KEY_ALIAS").orNull
                keyPassword = providers.environmentVariable("GASUKOM_KEY_PASSWORD").orNull
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.findByName("rilis") ?: signingConfigs.getByName("debug")
        }
    }

    packaging {
        // Pustaka native ML Kit dikompres di dalam APK supaya ukuran unduhan lebih kecil.
        jniLibs {
            useLegacyPackaging = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.mlkit.text.recognition)
    implementation(libs.mlkit.image.labeling)

    testImplementation(libs.junit)
}
