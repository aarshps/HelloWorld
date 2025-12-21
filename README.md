# Varisankya - Smart Subscription Manager

Varisankya is a modern, high-performance Android application designed to help users track and manage their recurring subscriptions with ease. Built with a focus on the **Material 3 Expressive (M3E)** design language, it offers a tactile, personalized, and visually stunning experience.

## ✨ Latest Release: v1.3

This release marks a significant milestone in UX/UI refinement and functional stability.

### What's New in v1.3
*   **Material 3 Expressive Audit**: Full overhaul of the UI/UX to follow the latest M3 guidelines, including filled card styles, expressive typography, and refined spacing.
*   **Intelligent Search**: Added auto-focused search bars in all selection modals (Currency, Period, Category) with real-time filtering.
*   **Enhanced Payment Timeline**: A vertical timeline view for payment history with automated "No History" states.
*   **Smart Schedule Shifting**: "Change Schedule & Pay" now correctly recalculates future due dates based on your custom payment date.
*   **Expressive Navigation**: Standardized haptics (Confirm, Virtual Key) and an `ExtendedFloatingActionButton` with scroll-aware collapse behavior.
*   **Bug Fixes**: Resolved Firestore permission issues and corrected sub-collection access patterns for reliable syncing.

## 🚀 Key Features

*   **Subscription Tracking**: Effortlessly manage costs, recurrence, and due dates.
*   **Daily Reminders**: Integrated `WorkManager` notifications for upcoming payments.
*   **Usage-Based Personalization**: The app learns your preferences, bubbling up your most used currencies and categories.
*   **Secure Authentication**: Google Sign-In integration for seamless cloud sync.
*   **Cloud Architecture**: Real-time data persistence with Firebase Firestore.
*   **Design First**:
    *   Material You Dynamic Color support.
    *   Haptic Feedback for every meaningful interaction.
    *   Dark Mode by default.

## 🛠 Technical Specifications

*   **Platform**: Android 15+ (API 35+)
*   **Language**: 100% Kotlin
*   **UI Framework**: Material Components 1.11.0 (Material 3)
*   **Backend**: Firebase (Auth & Firestore)
*   **Worker API**: Android WorkManager

## 📦 Build & Release

To build the debug APK manually, run:
```bash
./gradlew :app:assembleDebug
```
The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

### Tagging Version v1.3
To freeze this state in Git:
```bash
git add .
git commit -m "chore: freeze state for v1.3 release"
git tag -a v1.3 -m "Release version 1.3"
git push origin v1.3
```

## ⚙️ Setup

1.  Place your `google-services.json` in the `app/` folder.
2.  Ensure Firestore Rules allow sub-collection access:
    ```
    match /users/{userId}/{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    ```
3.  Sync and Run from Android Studio.

## 📜 License
This project is licensed under the MIT License.
