// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.9.3" apply false
    id("org.jetbrains.kotlin.android") version "2.2.0" apply false
    id("com.google.dagger.hilt.android") version "2.57" apply false
    id("com.google.devtools.ksp") version "2.2.0-2.0.2" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0" apply false
    id("com.google.protobuf") version "0.9.5" apply false
    id("org.jetbrains.kotlin.jvm") version "2.2.0" apply false
    id("org.sonarqube") version "6.3.1.5724" apply false
}
