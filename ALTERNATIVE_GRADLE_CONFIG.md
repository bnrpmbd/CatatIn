# ALTERNATIVE GRADLE CONFIGURATION
# Jika masih error, ganti gradle-wrapper.properties dengan:

distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.0-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists

# Dan ganti build.gradle (root) dengan:
plugins {
    id 'com.android.application' version '8.0.2' apply false
    id 'org.jetbrains.kotlin.android' version '1.8.20' apply false
    id 'org.jetbrains.kotlin.kapt' version '1.8.20' apply false
}