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
    // App dependencies
    implementation("org.greenrobot:eventbus:3.3.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.maps.android:android-maps-utils:2.3.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation("com.google.firebase:firebase-auth:23.2.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.common)
    implementation("com.github.yalantis:ucrop:2.2.6")
    implementation(libs.espresso.contrib)

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation(libs.junit.jupiter)
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.mockito:mockito-inline:4.11.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("org.robolectric:robolectric:4.8.1")

    // Android test dependencies
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test:monitor:1.6.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("org.mockito:mockito-android:4.11.0")


    // Debug dependencies
    debugImplementation("androidx.fragment:fragment-testing:1.5.5")
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-firestore:25.1.2")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
}

configurations.all {
    resolutionStrategy {
        force("androidx.test:core:1.5.0")
        force("androidx.test:runner:1.5.2")
        force("androidx.test:rules:1.5.0")
        force("androidx.test:monitor:1.6.0")
        force("androidx.test.ext:junit:1.1.5")
        force("androidx.test.espresso:espresso-core:3.5.1")
        force("androidx.test.espresso:espresso-intents:3.5.1")

        // Force the use of protobuf-javalite
        force("com.google.protobuf:protobuf-javalite:3.25.1")
        eachDependency {
            if (requested.group == "com.google.protobuf" && requested.name == "protobuf-lite") {
                useTarget("com.google.protobuf:protobuf-javalite:3.25.1")
            }
        }
    }
}
