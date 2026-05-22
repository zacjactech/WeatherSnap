# WeatherSnap 🌤️📸

WeatherSnap is a polished, production-grade Android application built to search live weather for cities, create custom weather reports with captured photos, and save them locally. It strictly adheres to modern Android architecture principles and is built entirely with Jetpack Compose.

pus## ✨ Features

- **Live Weather Search:** Instantly search for any city and get real-time weather data.
- **City Suggestions:** Autocomplete suggestions for cities as you type.
- **Detailed Weather Info:** Get all the essential weather details: temperature, condition, humidity, wind speed, and pressure.
- **Custom Camera:** A custom-built camera screen for a seamless photo-capturing experience.
- **Image Compression:** Captured images are automatically compressed to save storage space.
- **Local Reports:** Save your weather reports with notes and photos directly on your device.
- **View Saved Reports:** A dedicated screen to view and manage all your saved weather reports.
- **Responsive UI:** A clean and modern UI that adapts to different screen sizes.

## 🎬 Screen Recording

*(This is a placeholder for the screen recording of the full app flow, as required by the assignment.)*

---

## 🛠️ Tech Stack & Architecture

### Tech Stack

- **Kotlin & Coroutines/Flow** for asynchronous data streaming.
- **Jetpack Compose** for all UI components.
- **MVVM Architecture** enforced via Hilt Dependency Injection.
- **Room Database** for local caching and the Draft state.
- **Retrofit & OkHttp** for networking and logging.
- **CameraX** for the custom camera implementation.
- **Material 3** for premium styling, typography, and color schemes.
- **Gson** for JSON parsing.
- **Coil** for image loading.

### Architecture

This app follows the **MVVM (Model-View-ViewModel)** architecture pattern, which is the standard for modern Android development. This separation of concerns makes the app more scalable, maintainable, and testable.

- **View:** The UI is built entirely with **Jetpack Compose**, a declarative UI toolkit.
- **ViewModel:** The ViewModels are responsible for holding and managing UI-related data in a lifecycle-conscious way.
- **Model:** The Model represents the data layer, which is handled by **Room Database** for local storage and **Retrofit** for remote data from the Open-Meteo API.

**Dependency Injection** is managed by **Hilt**, which simplifies the process of providing dependencies to different parts of the app.

---

## 🧠 Developer Judgment Challenge

**The Challenge:**
Protect the report creation flow from lifecycle and data-loss issues (e.g., rotating the device or backgrounding the app after capturing a photo and adding notes, but before hitting save). Duplicate reports must be avoided, weather snapshots must remain exact, and temporary image files must not leak.

**Our Approach: Database-Backed Drafts**
Instead of relying solely on `SavedStateHandle` or `ViewModel` in-memory state, we implemented a robust **Offline-First Draft mechanism** utilizing Room Database as the single source of truth.

1.  **Immediate Draft Creation:** When a user taps "Create Report", we immediately insert a `WeatherSnapEntity` into Room with a status of `DRAFT`. This snapshot contains the exact, frozen weather details at that millisecond.
2.  **Navigation by ID:** The `CreateReportScreen` and `CameraScreen` are driven by passing the unique `draft_id` via Jetpack Navigation parameters.
3.  **Reactive UI State:** The `ReportViewModel` loads the draft from Room into a `StateFlow`. When the user types notes or captures an image, the Room database is incrementally updated. If the device rotates, the app is killed, or it goes into the background, the UI instantly re-subscribes to the exact same `draft_id` upon recreation. No data is lost, and because it's the exact same row ID, no duplicates are ever created.
4.  **Finalizing:** When "Save" is clicked, we merely update the database row status from `DRAFT` to `SAVED` (or `PENDING` for sync simulation).
5.  **No File Leaks:** If the user deletes or cancels a report, the `SnapDetailViewModel` commands the `FileStorageManager` to explicitly delete the local `File` paths associated with that draft before removing the DB record. Furthermore, orphaned `DRAFT` records (e.g., if the app is force-killed) are garbage-collected by the background `WeatherSyncWorker`.

**Tradeoffs:**

-   **Pros:** Extremely robust. Immune to process death (`ViewModel` clearing). Safely anchors large binary data (photos) to a persistent file path rather than keeping bitmaps in RAM during rotation.
-   **Cons:** Slightly higher disk I/O overhead since we write to SQLite on text-field changes, but this is mitigated by running all operations on `Dispatchers.IO` to ensure UI threads remain perfectly smooth.

---

## 🚀 Setup & Run Instructions

### Prerequisites

-   **Android Studio** (Koala or newer recommended)
-   **JDK 17**
-   **Android SDK API 34**
-   A physical device or an emulator running Android 8.0+ (API 26+)

### Running the App

1.  Clone or unzip this repository.
2.  Open the project in Android Studio.
3.  Allow Gradle to sync. No external API keys are required (we use the free Open-Meteo API).
4.  Run the `:app` configuration targeting your connected device or emulator.
5.  Alternatively, run via terminal:
    ```bash
    ./gradlew assembleDebug
    adb install app/build/outputs/apk/debug/app-debug.apk
    ```
