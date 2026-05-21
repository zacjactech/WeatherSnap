# Repository Guidelines

## Project Structure & Module Organization

WeatherSnap is a modular Android app built with Kotlin, Jetpack Compose, Hilt, Room, Retrofit, and WorkManager. The `:app` module wires navigation, permissions, dependency injection, and background sync. Feature UI lives in `feature/*` modules such as `feature/weather`, `feature/camera`, `feature/report`, `feature/history`, and `feature/settings`.

Shared layers live under `core/*`: `core/domain` for use cases and repository contracts, `core/model` for shared models, `core/database` for Room persistence, `core/network` for Retrofit APIs, `core/file` for file storage, `core/designsystem` for theme assets, and `core/common` for utilities. Gradle convention plugins are in `build-logic/convention`. Design references are in `stitch/`; planning docs are in `docs/`.

## Build, Test, and Development Commands

- `./gradlew assembleDebug`: builds the debug APK.
- `./gradlew testDebugUnitTest`: runs local JVM unit tests for debug variants.
- `./gradlew connectedDebugAndroidTest`: runs instrumented Android tests on a connected emulator or device.
- `./gradlew lintDebug`: runs Android lint for the debug variant.
- `./gradlew clean`: removes Gradle build outputs.

Use JDK 17 and Android SDK API 34. Prefer the checked-in Gradle wrapper over a system Gradle install.

### Waydroid Device (primary test target)

The development machine runs Waydroid as the Android test device. ADB is pre-connected; use these commands for fast iteration:

- **ADB target**: `192.168.240.112:5555` (Waydroid IP; verify with `waydroid status` if it changes)
- **App package**: `com.weather.snap`, main activity: `com.weather.snap/.MainActivity`
- **Connect ADB**: `adb connect 192.168.240.112:5555`
- **Install latest debug APK**: `adb -s 192.168.240.112:5555 install -r app/build/outputs/apk/debug/app-debug.apk`
- **Launch app**: `adb -s 192.168.240.112:5555 shell am start -n com.weather.snap/.MainActivity`
- **Full rebuild + install shortcut**: `./gradlew assembleDebug && adb -s 192.168.240.112:5555 install -r app/build/outputs/apk/debug/app-debug.apk`
- **Logcat (WeatherSnap only)**: `adb -s 192.168.240.112:5555 logcat -s WeatherSnap:V AndroidRuntime:E`
- **Waydroid install** (alternative): `waydroid app install <apk-path>`

**Faster iteration with Compose Live Edit**: Connect ADB as above, open the project in Android Studio, select `192.168.240.112:5555` as the run target, and run the app once. Then enable Live Edit (`Settings → Editor → Live Edit` or the status bar toggle) — Compose UI changes hot-reload on the device as you type without a rebuild. Use **Apply Changes** (⚡) for non-Compose code changes.

### Adding a new module

Apply convention plugins in `build.gradle.kts`: `weathersnap.android.application` (or `weathersnap.android.library`), `weathersnap.android.compose`, `weathersnap.android.hilt`. Register the module in `settings.gradle.kts`. Do not add `repositories {}` blocks inside modules — `RepositoriesMode.FAIL_ON_PROJECT_REPOS` enforces root-only repos.

## Coding Style & Naming Conventions

Use Kotlin with 4-space indentation and idiomatic Compose patterns. Keep module boundaries strict: feature modules depend on domain/design/common APIs, while data implementations stay in `core/database`, `core/network`, or `core/file`. Name composables with PascalCase nouns ending in `Screen`, `Content`, or a specific UI role; name ViewModels as `FeatureViewModel`; name UI state holders as `FeatureUiState`.

### Build conventions

- Hilt uses KSP for code generation (`ksp` configuration), not KAPT.
- Room entities also require KSP — annotate with `@Entity` and add `ksp(libs.room.compiler)`.
- Compose compiler version is managed centrally via `libs.versions.toml` (`composeCompiler`).
- `minSdk = 26`, `compileSdk = 34`, JVM target 17 — configured in `build-logic/convention`.
- Release builds enable R8 minification and resource shrinking (`proguard-rules.pro`).

## Testing Guidelines

Place JVM tests in `src/test/java` and instrumented tests in `src/androidTest/java` within the owning module. Use JUnit, MockK, Turbine, and `kotlinx-coroutines-test` for ViewModels, flows, use cases, and repository contracts. Test names should describe behavior, for example `syncWeatherSnaps_marksFailedUploadsAsFailed`. Run `./gradlew testDebugUnitTest` before opening a PR; add `connectedDebugAndroidTest` when touching Room, Android framework code, camera, or Compose UI behavior.

## Commit & Pull Request Guidelines

Recent history uses Conventional Commits, such as `refactor: ...` and `chore: ...`. Follow `type: concise imperative summary` with types like `feat`, `fix`, `refactor`, `test`, `docs`, and `chore`.

PRs should include a short summary, affected modules, verification commands run, linked issues when applicable, and screenshots or recordings for UI changes. Note any migration, permission, or configuration impact.

## Security & Configuration Tips

Do not commit local secrets or `.vscode/mcp.json`; use `.vscode/mcp.json.template` as the starting point. Keep API keys out of source.
