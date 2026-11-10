# One A Run

One A Run is an Android app for setting up and running head-to-head races. Runners create a session,
share a join code, and race while the app tracks GPS location and displays live progress.

## Features

- Create a race session with a runner name and target distance.
- Invite another runner with a shareable session code.
- Join a session from another device using its code.
- Track race distance and pace using GPS, with race updates and results exchanged through MQTT.
- Built with Kotlin and Jetpack Compose.

The app relies on the One A Run backend for session management and race processing. See
the [backend project](https://github.com/luisfelipegraciolli/One-a-Run-Backend) for its API and
implementation details.

## Tech stack

- **Kotlin**, Coroutines and Flow
- **Jetpack Compose** with Material 3 and **Navigation Compose**
- **MVVM** with `StateFlow` and manual dependency injection (`AppContainer` +
  `ViewModelProvider.Factory`)
- **Retrofit** + **Gson** for the REST API
- **HiveMQ MQTT Client** (MQTT 5) for real-time messaging
- **Fused Location Provider** (Google Play services) for GPS

## Get started

### Requirements

- Android Studio and Android SDK Platform 37.
- JDK 21. The Gradle wrapper is configured to use Gradle 9.4.1.
- A running One A Run backend and internet access to the configured MQTT broker.
- An Android device or emulator with location services enabled. The app requests location permission
  when opened.

### Configure the backend connection

The API base URL is currently set in [
`AppContainer.kt`](app/src/main/java/com/shiwa/onearun/data/container/AppContainer.kt). Change it to
an address reachable from the device, keeping the trailing slash:

```kotlin
private val baseUrl = "http://10.0.2.2:8080/"
```

`10.0.2.2` is the Android emulator's address for reaching a backend running on the development
machine. For a physical device, use the machine's LAN address instead. The MQTT broker is configured
in the same file (`broker.emqx.io:1883`).

The app currently permits cleartext HTTP for local development. Use HTTPS for a production API.

### Build and run

Clone the repository and build the debug app:

```bash
git clone https://github.com/luisfelipegraciolli/One-A-Run-Android.git
cd One-A-Run-Android
./gradlew assembleDebug
```

Install it on a connected device or running emulator:

```bash
./gradlew installDebug
```

Alternatively, open the repository in Android Studio, sync Gradle, select a device, and run the
`app` configuration.

### Run tests

Run the Android unit tests with:

```bash
./gradlew testDebugUnitTest
```

## Roadmap

- [ ] Map with both runners' routes on the finish screen
- [ ] Server-provided start time, so both stopwatches start in sync
- [ ] Foreground service to keep tracking with the screen off
- [ ] Authentication and broker access control

## Help and documentation

- [Report a bug or request a feature](https://github.com/luisfelipegraciolli/One-A-Run-Android/issues).
- See the [One A Run backend](https://github.com/luisfelipegraciolli/One-a-Run-Backend) for backend
  and API information.
- Review [`app/build.gradle.kts`](app/build.gradle.kts) for Android SDK settings and app
  dependencies, and [`gradle/libs.versions.toml`](gradle/libs.versions.toml) for dependency
  versions.

## Maintainers and contributing

One A Run is maintained by [Luis Graciolli](https://github.com/luisfelipegraciolli). Contributions
are welcome: open an issue to discuss substantial changes, then submit a pull request with a clear
description and relevant tests. Run `./gradlew testDebugUnitTest` before submitting.
