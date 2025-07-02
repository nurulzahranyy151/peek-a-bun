// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Ensure the Google Services plugin classpath is included
        classpath("com.google.gms:google-services:4.4.2")
    }
}



tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}