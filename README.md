# One A Run

One A Run is an Android prototype for setting up a head-to-head running session. A runner can create a session with a name and distance, share its join code, and have another runner join from their device.

## Features

- Create a race session and receive a shareable join code.
- Join an existing session with a runner name and join code.
- Check for a joining runner using the race API, with MQTT status updates and polling while waiting.
- Built with Kotlin and Jetpack Compose.

The session creation and joining flow is connected to a backend. The lobby, countdown, and race screens are still placeholders; live race tracking is not implemented yet.

## Getting started

### Requirements

- Android Studio with Android SDK Platform 37 installed.
- JDK 21 (the Gradle daemon toolchain is configured for Java 21).
- A running One A Run backend reachable from the Android device or emulator. The app expects the API paths defined in [RaceApiService.kt](app/src/main/java/com/shiwa/onearun/data/remote/api/RaceApiService.kt).
- Internet access to reach the configured MQTT broker.

### Configure the backend

The API base URL is currently hard-coded in [AppContainer.kt](app/src/main/java/com/shiwa/onearun/data/container/AppContainer.kt). Before running the app, change it to the address of your backend:

```kotlin
private val baseUrl = "http://192.168.1.20:8080/"
```

Use an address reachable from the device. For an Android emulator connecting to a backend running on the development machine, `http://10.0.2.2:8080/` is typically used; for a physical device, use the machine's LAN address. Keep the trailing slash. The app currently uses cleartext HTTP, which is suitable only for local development; use HTTPS for a production backend.

The MQTT broker is configured separately in [AppContainer.kt](app/src/main/java/com/shiwa/onearun/data/container/AppContainer.kt) as `broker.emqx.io:1883`.

### Build and run

Clone the repository and open it in Android Studio, or build from the repository root with the Gradle wrapper:

```bash
git clone https://github.com/luisfelipegraciolli/One-A-Run-Android.git
cd One-A-Run-Android
./gradlew assembleDebug
```

To install the debug app on a connected device or running emulator:

```bash
./gradlew installDebug
```

You can also open the project in Android Studio, let Gradle sync, select an emulator or connected device, and run the `app` configuration.

### Run tests

```bash
./gradlew testDebugUnitTest
```

## Help and documentation

- Report a bug or request a feature in the [GitHub issue tracker](https://github.com/luisfelipegraciolli/One-A-Run-Android/issues).
- Check [app/build.gradle.kts](app/build.gradle.kts) for Android SDK settings and app dependencies, and [gradle/libs.versions.toml](gradle/libs.versions.toml) for dependency versions.

## Maintainers and contributing

One A Run is maintained by [Luis Graciolli](https://github.com/luisfelipegraciolli).

Contributions are welcome. For substantial changes, open an issue first to discuss the approach. For a pull request, describe the change, include relevant tests, and run `./gradlew testDebugUnitTest` before submitting. Please keep contributions focused and follow the existing Kotlin and Jetpack Compose patterns.
