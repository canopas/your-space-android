// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.3.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.android.library") version "8.3.2" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("org.jlleitschuh.gradle.ktlint") version "11.4.2"
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
    id("com.google.devtools.ksp") version "1.9.23-1.0.20"
}
