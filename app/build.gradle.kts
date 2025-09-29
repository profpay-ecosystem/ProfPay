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
}

ktlint {
    version.set("1.3.1")
    debug.set(false)
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)

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
    ignoreFailures = false
    allRules = false
}

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
    namespace = "com.example.telegramWallet"
    compileSdk = 36

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: project.findProperty("KEYSTORE_FILE") as String)
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: project.findProperty("KEYSTORE_PASSWORD") as String
            keyAlias = System.getenv("KEY_ALIAS")  ?: project.findProperty("KEY_ALIAS") as String
            keyPassword = System.getenv("KEY_PASSWORD") ?: project.findProperty("KEY_PASSWORD") as String
        }
    }

    defaultConfig {
        applicationId = "com.example.telegramWallet"
        minSdk = 29
        targetSdk = 35
        versionCode = 5

//        MAJOR: Внесение изменений, ломающих обратную совместимость.
//        MINOR: Добавление новых функций без нарушения совместимости.
//        PATCH: Исправление ошибок и незначительные улучшения без изменения функциональности.
        versionName = "5.10.29" // MAJOR.MINOR.PATCH

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
                val versionCode = variant.versionCode

                val tag = "beta"

                val outputFileName = "profpay-$versionName-${tag}$versionCode.apk"
                output.outputFileName = outputFileName
            }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
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
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.5")
        classpath("com.google.gms:google-services:4.4.3")
        classpath(kotlin("serialization", version = "1.9.21"))
        classpath("dev.detekt:detekt-gradle-plugin:2.0.0-alpha.0")
    }
}

dependencies {
//    implementation(project(":walletcore"))

    implementation(files("libs/bitcoinj-core-0.17-SNAPSHOT.jar"))
    val navVersion = "2.9.2"
    val roomVersion = "2.7.2"
    val lifecycleVersion = "2.9.2"
    val grpcVersion = "1.73.0"

    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.2")
    // Алгоритм шифрования
    implementation("org.mindrot:jbcrypt:0.4")

//    biometric
    implementation("androidx.biometric:biometric:1.1.0")

    // gRPC libs
    implementation("com.google.protobuf:protobuf-java:4.31.1")
    implementation("com.google.protobuf:protobuf-java-util:4.31.1")

    implementation("io.grpc:grpc-netty-shaded:1.73.0")
    implementation("io.grpc:grpc-netty:1.73.0")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("io.grpc:grpc-okhttp:$grpcVersion")

    implementation("io.github.tronprotocol:trident:0.10.0")
    implementation(files("libs/bitcoinj-core-0.17-SNAPSHOT.jar"))

    implementation("androidx.navigation:navigation-compose:$navVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("com.google.dagger:hilt-android:2.57")
    kapt("com.google.dagger:hilt-android-compiler:2.57")
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    // Kotlin
    implementation("androidx.compose.runtime:runtime-livedata:1.8.3")

    // Room DataBase
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    testImplementation("androidx.room:room-testing:$roomVersion")
    implementation("androidx.room:room-paging:$roomVersion")

    //
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // bip39 mnemonic
    implementation("cash.z.ecc.android:kotlin-bip39:1.0.9")

    // JSON Parse
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // Krontab
    implementation("dev.inmo:krontab:2.7.2")

    // OkHttp Client
    implementation("com.squareup.okhttp3:okhttp:5.1.0")

    // iText7
    implementation("com.itextpdf:itext-core:9.2.0")

    // interactive tips
    implementation("ly.com.tahaben:showcase-layout-compose:1.0.9")

    // shapes(графика)
    implementation("androidx.graphics:graphics-shapes:1.0.1")

    // Контроллер UI
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.36.0")

    // Pusher Notification
    implementation("com.pusher:pusher-java-client:2.4.4")

    // QR-code builder
    implementation("com.google.zxing:core:3.5.3")

    // google-font
    implementation("androidx.compose.ui:ui-text-google-fonts:1.8.3")

    implementation("io.github.rizmaulana:compose-stacked-snackbar:1.0.4")

    implementation("me.pushy:sdk:1.0.124")

    implementation("io.sentry:sentry-android:8.19.1")
    implementation("io.sentry:sentry-compose-android:8.19.1")
    implementation("androidx.lifecycle:lifecycle-process:2.9.2")

    implementation("androidx.datastore:datastore:1.1.7")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.security:security-crypto:1.1.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("org.mockito:mockito-android:5.18.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("io.mockk:mockk:1.14.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.slf4j:slf4j-simple:2.0.17")
}

kapt {
    correctErrorTypes = true
}
