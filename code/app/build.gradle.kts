plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.hamidat.nullpointersapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hamidat.nullpointersapp"
        minSdk = 24
        targetSdk = 34
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
}

dependencies {
    implementation ("org.greenrobot:eventbus:3.3.1")
    implementation ("com.google.android.material:material:1.9.0")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.maps.android:android-maps-utils:2.3.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.common)
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}