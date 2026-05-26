# Panini FIFA WC 2026 Sticker Tracker (Colombian Release)

## Overview
This is a native Android application designed to track sticker collection progress for the Panini FIFA World Cup 2026 album, specifically targeted for the Colombian release (distributed by Continente). The application provides a fast, offline-first experience to manage a collection of 994+ stickers, including the introductory section, all national teams, and Coca-Cola exclusives.

The app is built using **Kotlin** and leverages the **Room Persistence Library** to ensure data is safely stored locally.

## Features
*   **Comprehensive Catalog:** Pre-loaded with the complete list of 994 stickers.
*   **Smart CV Scanner:** Use your phone's camera to scan the back of stickers for automatic identification. Features live visual feedback, top-right priority detection, and works 100% offline.
*   **Nested Accordion UI:** Easily navigate through the extensive catalog using a 3-level hierarchy (Category -> Nation -> Sticker).
*   **Real-Time Progress Tracking:** A persistent sticky header displays your total collection percentage.
*   **Collection Statistics:** A dedicated screen providing a deep dive into global completion, swap availability, top repeated stickers, and nation-by-nation progress with sorting capabilities.
*   **Advanced Search & Filtering:**
    *   Text search by Sticker Code, Name, or Country.
    *   Quick status filters: "All", "Owned", "Missing", and "Swaps".
*   **Fast Input Method:** Tap on the sticker count to open a Modal Bottom Sheet for quick, numeric entry without the system keyboard.
*   **Data Portability:** Export your collection progress as a lightweight JSON file and restore it easily using Android's Storage Access Framework (SAF).
*   **Official Branding:** Adheres to the official FIFA WC 2026 visual identity with distinct states for Missing, Owned, and Duplicated stickers.

## Development Process
This project is being developed autonomously by **Gemini CLI**, an interactive AI agent. The implementation follows human-provided specifications (see `app_specs.md` and `GEMINI.md`) and adheres to modern Android engineering standards. The agent is responsible for the entire lifecycle: architecture design, implementation, testing, and documentation.

## File Structure
*   `app/` - The main Android application module containing Kotlin source code, UI layouts, and resources.
*   `gradle/` - Gradle Wrapper configuration.
*   `master_list.md` - The master source of truth for all sticker data.
*   `ingest_data.py` - Python script used to convert `master_list.md` into the initial `catalog.db` SQLite database.
*   `GEMINI.md`, `app_specs.md`, `data_model.md`, `current_status.md` - Project architecture and status documentation.
*   `user_manual/` - Contains the multi-language user manual in Markdown and PDF formats, along with optimized screenshots.
*   `.gitignore` - Defines files excluded from version control.

## Compilation and Installation

### Prerequisites
*   **JDK 17** (or compatible version used by Gradle).
*   **Android SDK:** Compile SDK 35, Min SDK 24.
*   An Android device or emulator running Android 7.0 (API 24) or higher.

### Build Instructions
To compile the project and generate a debug APK, run the following command from the root directory:

```bash
./gradlew clean assembleDebug
```

The generated APK will be automatically named based on the version, for example: `PaniniWC26-v1.0b-debug.apk`.

### Installation
Ensure your Android device is connected with USB Debugging enabled, or an emulator is running. Install the generated APK using ADB:

```bash
adb install -r app/build/outputs/apk/debug/PaniniWC26-v1.0b-debug.apk
```

Alternatively, you can open the project in **Android Studio** and click the "Run" button.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
icense - see the [LICENSE](LICENSE) file for details.
