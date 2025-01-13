/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("checkstyle")
    id("maven-publish")
}

group = "com.github.vinhdn"
version = "1.0"

android {
    buildFeatures {
        aidl = true
        buildConfig = true
    }
    namespace = "de.blinkt.openvpn"
    compileSdk = 34
    //compileSdkPreview = "UpsideDownCake"

    // Also update runcoverity.sh
    ndkVersion = "28.0.12674087-rc2"


    defaultConfig {
        minSdk = 21
        targetSdk = 34
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets", "build/ovpnassets")
        }
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_17
        sourceCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
            isUniversalApk = true
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.vinhdn"
            artifactId = "openvpn_library"
            version = "1.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

dependencies {
    // https://maven.google.com/web/index.html
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    testImplementation(libs.androidx.test.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin)
    testImplementation(libs.mockito.core)
    testImplementation(libs.robolectric)

    implementation("com.github.jitpack:android-example:1.0.1")
}