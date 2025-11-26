plugins {
    id("com.android.application")
    id("kotlin-android")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

android {
    namespace = "com.example.hackathonflutter"

    // ALTERAÇÃO 1: Definindo SDK 36 explicitamente conforme exigido pelos plugins de câmera
    compileSdk = 36

    // ALTERAÇÃO 2: Definindo a versão exata do NDK exigida pelos plugins ML Kit
    ndkVersion = "27.0.12077973"

    compileOptions {
        // ALTERAÇÃO 3: Atualizando para Java 17 para alinhar com o SDK 36 e resolver conflitos
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        // ALTERAÇÃO 3: Alinhando o Kotlin também para a versão 17
        jvmTarget = "17"
    }

    defaultConfig {
        // TODO: Specify your own unique Application ID (https://developer.android.com/studio/build/application-id.html).
        applicationId = "com.example.hackathonflutter"
        // You can update the following values to match your application needs.
        // For more information, see: https://flutter.dev/to/review-gradle-config.
        minSdk = flutter.minSdkVersion
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName
    }

    buildTypes {
        release {
            // TODO: Add your own signing config for the release build.
            // Signing with the debug keys for now, so `flutter run --release` works.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

flutter {
    source = "../.."
}