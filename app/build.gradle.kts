plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
}

android {
    namespace = "com.efzyn.cekresiapp"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.efzyn.cekresiapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        // Mengambil API Key dari gradle.properties
        buildConfigField ("String", "BINDERBYTE_API_KEY", "\"${project.findProperty("BINDERBYTE_API_KEY") ?: ""}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // --- PENDEKATAN MODERN UNTUK MENGUBAH NAMA DASAR APK ---
    // Ini akan mempengaruhi SEMUA build type (debug, release, dll.)
    setProperty("archivesBaseName", "Cek_Resi_App-v${defaultConfig.versionName}")

    buildTypes {
        release {
            isMinifyEnabled = false // Untuk rilis produksi, pertimbangkan true.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Tidak perlu lagi blok applicationVariants.all di sini untuk mengubah nama file
            // jika setProperty("archivesBaseName", ...) sudah cukup.
        }
        debug {
            // Konfigurasi untuk build type debug bisa ditambahkan di sini jika perlu
            // Nama file debug juga akan mengikuti format dari setProperty("archivesBaseName", ...)
            // yaitu: CekResiAppUTS-v[versionName]-debug.apk
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit & Gson Converter (untuk networking dan parsing JSON)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3") // Untuk logging request/response (Hanya untuk development)

    // Coroutines (untuk asynchronous programming)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2") // ViewModel KTX
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2") // Lifecycle KTX for viewModelScope

    // RecyclerView (untuk menampilkan daftar)
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Material Design Components (untuk UI yang lebih baik)
    // implementation("com.google.android.material:material:1.11.0") // Sudah ada via alias(libs.material)

    // Glide (untuk memuat gambar logo kurir, jika ada URL dari API)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // SwipeRefreshLayout (untuk fitur refresh)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

}
