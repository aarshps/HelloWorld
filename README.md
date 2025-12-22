# Varisankya - Smart Subscription Manager

Varisankya is a modern, high-performance Android application designed to help users track and manage their recurring subscriptions with ease. Built with a focus on the **Material 3 Expressive (M3E)** design language, it offers a tactile, personalized, and visually stunning experience.

## ✨ Latest Release: v1.4

This release further refines the UX/UI, improves interaction models, and polishes the expressive design language.

### What's New in v1.4
*   **Intuitive Payment Editing**: Removed the dedicated edit icon in the payment history. Now, the entire row is clickable with a subtle ripple effect, making it faster and more intuitive to update past payment dates.
*   **Unified Visual Language**: Standardized the destructive action styling. The payment deletion icon now matches the subscription deletion icon's color (Error token) for a consistent and clear visual hierarchy.
*   **Fluid Motion & Transitions**: Integrated Material 3 Expressive alpha transitions. All state changes now feature smooth, non-stuttering fade animations, eliminating jarring layout shifts.
*   **Refined Typography**: Removed aggressive "hard" bolding throughout the app to align with the cleaner design language of Android 16 QPR 3.
*   **Immersive Haptic Feedback**: Subtle, tactile haptics integrated for scrolling, modal expansion, and all meaningful interactions.
*   **Intelligent Search**: Auto-focused search bars in all selection modals (Currency, Period, Category) with real-time filtering and a clean, icon-free design.
*   **Optimized Form Layouts**: 
    *   Single-line Due Date layout with grouped Frequency/Period fields.
    *   Streamlined button hierarchy for faster subscription entry and editing.
*   **Bug Fixes & Stability**: Resolved Pull-to-Refresh behavior and perfected chip spacing in selection modals.

## 🚀 Key Features

*   **Subscription Tracking**: Effortlessly manage costs, recurrence, and due dates.
*   **Daily Reminders**: Integrated `WorkManager` notifications for upcoming payments.
*   **Usage-Based Personalization**: The app learns your preferences, bubbling up your most used currencies and categories.
*   **Secure Authentication**: Google Sign-In integration for seamless cloud sync.
*   **Cloud Architecture**: Real-time data persistence with Firebase Firestore.

## 🛠 Technical Specifications

*   **Platform**: Android 15+ (API 35+)
*   **Language**: 100% Kotlin
*   **UI Framework**: Material Components 1.11.0 (Material 3)
*   **Backend**: Firebase (Auth & Firestore)

## 📦 Build & Release

To build the debug APK manually, run:
```bash
./gradlew :app:assembleDebug
```
The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

### Tagging Version v1.4
To freeze this state in Git:
```bash
git add .
git commit -m "chore: freeze state for v1.4 release"
git tag -a v1.4 -m "Release version 1.4"
git push origin v1.4
```

## ⚙️ Setup

1.  Place your `google-services.json` in the `app/` folder.
2.  Ensure Firestore Rules allow sub-collection access.
3.  Sync and Run from Android Studio.

## 📜 License
This project is licensed under the MIT License.
