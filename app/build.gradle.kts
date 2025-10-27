import com.google.protobuf.gradle.id
import dev.detekt.gradle.Detekt
import io.sentry.android.gradle.instrumentation.logcat.LogcatLevel

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.protobuf") version "0.9.5" apply true
    kotlin("plugin.serialization") version "2.2.0"
    id("io.sentry.android.gradle") version "5.8.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
    id("dev.detekt") version "2.0.0-alpha.0"
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
//    id("jacoco")
}

ktlint {
    version.set("1.3.1")
    debug.set(false)
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(true)

    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

tasks.withType<Detekt>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
        sarif.required.set(false)
        md.required.set(false)
    }
}

detekt {
    toolVersion = "2.0.0-alpha.0"

    source.setFrom("src/main/java", "src/main/kotlin")

    buildUponDefaultConfig = true
    ignoreFailures = true
    allRules = false
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "17"
    autoCorrect = true
}

// jacoco {
//    toolVersion = "0.8.10"
// }

sentry {
    tracingInstrumentation {
        enabled.set(true)

        logcat {
            enabled.set(true)
            minLevel.set(LogcatLevel.ERROR)
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

android {
    namespace = "com.profpay.wallet"
    compileSdk = 36

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: project.findProperty("KEYSTORE_FILE") as String)
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: project.findProperty("KEYSTORE_PASSWORD") as String
            keyAlias = System.getenv("KEY_ALIAS") ?: project.findProperty("KEY_ALIAS") as String
            keyPassword = System.getenv("KEY_PASSWORD") ?: project.findProperty("KEY_PASSWORD") as String
        }
    }

    defaultConfig {
        applicationId = "com.profpay.wallet"
        minSdk = 29
        targetSdk = 35
        versionCode = 6

//        MAJOR: Внесение изменений, ломающих обратную совместимость.
//        MINOR: Добавление новых функций без нарушения совместимости.
//        PATCH: Исправление ошибок и незначительные улучшения без изменения функциональности.
        versionName = "1.0.1" // MAJOR.MINOR.PATCH

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }

    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val versionName = variant.versionName

                val tag = "beta"

                val outputFileName = "profpay-$versionName-${tag}.apk"
                output.outputFileName = outputFileName
            }
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            signingConfig = signingConfigs.getByName("release")
            buildConfigField("Boolean", "IS_STAGING", "false")
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("Boolean", "IS_STAGING", "false")
        }
        create("staging") {
            initWith(getByName("release"))
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"

            signingConfig = signingConfigs.getByName("release")
            buildConfigField("Boolean", "IS_STAGING", "true")
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.6"
    }
    packaging {
        resources {
            excludes += "META-INF/**"
        }
    }
    sourceSets {
        getByName("main") {
            java.srcDir("src/main/java")
            resources.srcDir("src/main/resources")
            val protoSrcDir = "src/main/proto"
            java.srcDirs(protoSrcDir)
            resources.srcDirs(protoSrcDir)
        }

        getByName("androidTest") {
            java.srcDir("src/androidTest/java")
            resources.srcDir("src/androidTest/resources")
        }
    }
}

kotlin {
    jvmToolchain(17)
}

project.configurations.configureEach {
    resolutionStrategy {
        force("androidx.emoji2:emoji2-views-helper:1.3.0")
        force("androidx.emoji2:emoji2:1.3.0")
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.30.2"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.72.0"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc")
                id("java")
            }
        }
    }
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.protobuf.gradle.plugin)
        classpath(libs.google.services)
        classpath(kotlin("serialization", version = "1.9.21"))
        classpath(libs.detekt.gradle.plugin)
    }
}

dependencies {
    // -------------------------------------------------
    // UI (Compose, Material, Navigation, System UI)
    // -------------------------------------------------
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.showcase.layout.compose)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.compose.stacked.snackbar)

    // -------------------------------------------------
    // Dependency Injection (Hilt)
    // -------------------------------------------------
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // -------------------------------------------------
    // Lifecycle / State management
    // -------------------------------------------------
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.androidx.lifecycle.process)

    // -------------------------------------------------
    // Data layer (DB, Coroutines, Serialization, Network)
    // -------------------------------------------------
    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)

    // Coroutines + Network
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // -------------------------------------------------
    // Security / Storage
    // -------------------------------------------------
    implementation(libs.jbcrypt)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.datastore.preferences)
    implementation(libs.androidx.security.crypto)

    // -------------------------------------------------
    // Blockchain / Crypto
    // -------------------------------------------------
    implementation(files("libs/bitcoinj-core-0.17-SNAPSHOT.jar"))
    implementation(libs.trident)
    implementation(libs.kotlin.bip39)

    // -------------------------------------------------
    // gRPC / Protobuf
    // -------------------------------------------------
    implementation(libs.protobuf.java)
    implementation(libs.protobuf.java.util)
    implementation(libs.grpc.netty.shaded)
    implementation(libs.grpc.netty)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.okhttp)

    // -------------------------------------------------
    // External SDKs / Features (PDF, QR, Notifications)
    // -------------------------------------------------
    implementation(libs.itext.core)
    implementation(libs.pusher.java.client)
    implementation(libs.google.zxing.core)
    implementation(libs.pushy.sdk)

    // -------------------------------------------------
    // Monitoring / Logging
    // -------------------------------------------------
    implementation(libs.sentry.android)
    implementation(libs.sentry.sentry.compose.android)
    implementation(libs.slf4j.simple)
    implementation(libs.javax.annotation.api)

    // -------------------------------------------------
    // Scheduling
    // -------------------------------------------------
    implementation(libs.krontab)

    // -------------------------------------------------
    // Testing
    // -------------------------------------------------
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.room.testing)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.mockito.android)

    // -------------------------------------------------
    // Debug
    // -------------------------------------------------
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    testRuntimeOnly(libs.protobuf.java)
    testRuntimeOnly(libs.grpc.stub)
    testRuntimeOnly(libs.grpc.netty)
    testRuntimeOnly(libs.grpc.protobuf)

    // -------------------------------------------------
    // Annotation Processor
    // -------------------------------------------------
    annotationProcessor(libs.room.compiler)
}

kapt {
    correctErrorTypes = true
}
