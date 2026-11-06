# Verbecc for Android

[![Android CI](https://github.com/bretttolbert/verbecc-android/actions/workflows/android.yml/badge.svg)](https://github.com/bretttolbert/verbecc-android/actions/workflows/android.yml)

An offline verb conjugation app for Android, built with Kotlin, Jetpack Compose,
and Material 3. Conjugation is powered by
[verbecc-java](https://github.com/bretttolbert/verbecc-java).

## Features

- Conjugate verbs in **Catalan, Spanish, French, Italian, Portuguese, and Romanian**.
- Browse results organized by mood and tense.
- Select and copy conjugated forms.
- Use the app without an internet connection.
- Dark theme with readable Material 3 controls.
- Input validation and feedback for unknown verbs.
- Preserve the selected language and input when the activity is recreated.

## Usage

1. Select a language.
2. Enter an infinitive verb, such as `manger` for French or `tener` for Spanish.
3. Tap **Conjugate**, or submit using the keyboard.
4. Scroll through the verb's moods, tenses, and forms.

The app requires Android 7.0 (API 24) or newer.

## Development setup

### Requirements

- JDK 25.
- Android Studio with support for Android Gradle Plugin 9.5.1, or Android SDK
  command-line tools.
- Android SDK Platform 37.0 (`platforms;android-37.0` in `sdkmanager`).
- An Android emulator or device for instrumentation tests.

The project includes the Gradle 9.6.0 wrapper; a separate Gradle installation is
not required.

### Clone both projects

The [Gradle settings](./settings.gradle.kts) include `../verbecc-java` as a
composite build. Clone the Java library beside the Android project:

```sh
git clone https://github.com/bretttolbert/verbecc-java.git
git clone https://github.com/bretttolbert/verbecc-android.git
cd verbecc-android
```

The directory layout must be:

```text
parent/
  verbecc-android/
  verbecc-java/
```

Gradle builds the local Java dependency automatically; no separate installation
or publication to a Maven repository is necessary.

Open `verbecc-android` in Android Studio and let Gradle sync. For command-line
builds, configure the Android SDK location using `ANDROID_HOME` or a local
`local.properties` file containing `sdk.dir=/path/to/android-sdk`.

## Build and install

Build the debug APK:

```sh
./gradlew :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

Install it on a connected emulator or device:

```sh
./gradlew :app:installDebug
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Testing

Run local unit tests:

```sh
./gradlew :app:testDebugUnitTest
```

Run instrumentation tests with an emulator or device connected:

```sh
./gradlew :app:connectedDebugAndroidTest
```

Instrumentation tests cover conjugation in all six languages, input validation,
recovery from unknown verbs, and activity recreation. The connected test task
runs on all connected devices; to target one device, set `ANDROID_SERIAL` to its
serial before running the command.

## Continuous integration

The [Android CI workflow](./.github/workflows/android.yml) runs on pushes, pull
requests, and manual dispatch. It checks out both projects, configures JDK 25
and Android SDK Platform 37.0, and runs unit and instrumentation tests on an
API 36 x86_64 emulator. Test reports are uploaded as the
`android-test-reports` artifact when available.

## License

### GNU Lesser General Public License v3.0

This project is licensed under the GNU LGPL v3.0. See [LICENSE](./LICENSE).

The generated Gradle wrapper scripts retain their upstream Apache-2.0 notices.
Dependencies and their bundled data remain subject to their respective licenses;
see the [verbecc-java repository](https://github.com/bretttolbert/verbecc-java)
for its license and data notices.
