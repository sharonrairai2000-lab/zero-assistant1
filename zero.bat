@echo off
REM Create Zero project folder structure

mkdir Zero
cd Zero

REM Create app folders
mkdir app\src\main\java\com\example\zero
mkdir app\src\main\res\layout
mkdir app\src\main\res\values

REM Create GitHub Actions folder
mkdir .github\workflows

REM Create Gradle wrapper folder
mkdir gradle\wrapper

REM Root build.gradle
(
echo buildscript {
echo.    repositories {
echo.        google()
echo.        mavenCentral()
echo.    }
echo.    dependencies {
echo.        classpath "com.android.tools.build:gradle:8.1.1"
echo.    }
echo }
echo.
echo allprojects {
echo.    repositories {
echo.        google()
echo.        mavenCentral()
echo.    }
echo }
) > build.gradle

REM settings.gradle
echo rootProject.name = "Zero" > settings.gradle
echo include ':app' >> settings.gradle

REM gradle-wrapper.properties
(
echo distributionBase=GRADLE_USER_HOME
echo distributionPath=wrapper/dists
echo distributionUrl=https\://services.gradle.org/distributions/gradle-8.1.1-all.zip
echo zipStoreBase=GRADLE_USER_HOME
echo zipStorePath=wrapper/dists
) > gradle\wrapper\gradle-wrapper.properties

REM App build.gradle
(
echo plugins {
echo.    id 'com.android.application'
echo }
echo.
echo android {
echo.    compileSdk 33
echo.
echo.    defaultConfig {
echo.        applicationId "com.example.zero"
echo.        minSdk 24
echo.        targetSdk 33
echo.        versionCode 1
echo.        versionName "1.0"
echo.    }
echo.
echo.    buildTypes {
echo.        release { minifyEnabled false }
echo.        debug { debuggable true }
echo.    }
echo }
echo.
echo dependencies {
echo.    implementation 'androidx.appcompat:appcompat:1.6.1'
echo.    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
echo }
) > app\build.gradle

REM MainActivity.java
(
echo package com.example.zero;
echo.
echo import android.os.Bundle;
echo import androidx.appcompat.app.AppCompatActivity;
echo.
echo public class MainActivity extends AppCompatActivity {
echo.    @Override
echo.    protected void onCreate(Bundle savedInstanceState) {
echo.        super.onCreate(savedInstanceState);
echo.        setContentView(R.layout.activity_main);
echo.    }
echo }
) > app\src\main\java\com\example\zero\MainActivity.java

REM activity_main.xml
(
echo ^<?xml version="1.0" encoding="utf-8"?^>
echo ^<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android" xmlns:app="http://schemas.android.com/apk/res-auto" android:layout_width="match_parent" android:layout_height="match_parent"^>
echo ^</androidx.constraintlayout.widget.ConstraintLayout^>
) > app\src\main\res\layout\activity_main.xml

REM strings.xml
(
echo ^<resources^>
echo.    ^<string name="app_name"^>Zero^</string^>
echo ^</resources^>
) > app\src\main\res\values\strings.xml

REM AndroidManifest.xml
(
echo ^<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.example.zero"^>
echo.
echo.    ^<application android:allowBackup="true" android:label="Zero" android:icon="@mipmap/ic_launcher" android:roundIcon="@mipmap/ic_launcher_round" android:supportsRtl="true" android:theme="@style/Theme.AppCompat.Light.NoActionBar"^>
echo.        ^<activity android:name=".MainActivity"^>
echo.            ^<intent-filter^>
echo.                ^<action android:name="android.intent.action.MAIN"/^>
echo.                ^<category android:name="android.intent.category.LAUNCHER"/^>
echo.            ^</intent-filter^>
echo.        ^</activity^>
echo.    ^</application^>
echo ^</manifest^>
) > app\src\main\AndroidManifest.xml

REM GitHub Actions workflow
(
echo name: Android Debug Build
echo.
echo on:
echo.  push:
echo.    branches:
echo.      - main
echo.  workflow_dispatch:
echo.
echo jobs:
echo.  build:
echo.    runs-on: ubuntu-latest
echo.
echo.    steps:
echo.      - name: Checkout repository
echo.        uses: actions/checkout@v4
echo.
echo.      - name: Set up JDK 17
echo.        uses: actions/setup-java@v4
echo.        with:
echo.          java-version: 17
echo.          distribution: temurin
echo.
echo.      - name: Set up Android SDK
echo.        uses: android-actions/setup-android@v2
echo.        with:
echo.          api-level: 33
echo.          build-tools: 33.0.2
echo.          cache: true
echo.
echo.      - name: Cache Gradle packages
echo.        uses: actions/cache@v4
echo.        with:
echo.          path: |
echo.            ~/.gradle/caches
echo.            ~/.gradle/wrapper
echo.          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
echo.          restore-keys: |
echo.            ${{ runner.os }}-gradle-
echo.
echo.      - name: Grant execute permission for gradlew
echo.        run: chmod +x gradlew
echo.
echo.      - name: Build with Gradle
echo.        run: ./gradlew assembleDebug --stacktrace --info
echo.
echo.      - name: Upload APK
echo.        uses: actions/upload-artifact@v4
echo.        with:
echo.          name: Zero-debug
echo.          path: app/build/outputs/apk/debug/app-debug.apk
) > .github\workflows\android-build.yml

echo Zero project structure created successfully!
pause
