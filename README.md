# Varisankya - Smart Subscription Manager

Varisankya is a modern, high-performance Android application designed to help users track and manage their recurring subscriptions with ease. Built with a focus on the **Material 3 Expressive (M3E)** design language, it offers a tactile, personalized, and visually stunning experience.

## ✨ Latest Release: v1.4

This release achieves the final vision for visual clarity, tactile depth, and a premium "Black & White" expressive aesthetic using system dynamic colors.

### What's New in v1.4
*   **Themed Vector Branding**: Transitioned from bitmap logos to a clean, expressive vector logo (`ic_app_logo`). This allows the icon to intelligently flip between Black and White based on the system theme using `colorOnSurface` dynamic tinting, ensuring a premium, consistent look across the entire app.
*   **Concise Notifications**: Shortened notification titles to "Due" instead of "Subscription Due" to prevent text truncation in the notification shade, ensuring critical information is always visible.
*   **Expressive Swipe-to-Refresh**: Updated the pull-down-to-refresh component with Material 3 Expressive tokens (`colorSurfaceContainerHigh` and `colorPrimary`) for a high-contrast, integrated look that matches the app's refined surface containers.
*   **Ultra-Light Protruding Pillar**: Refined the unified status pillar to be significantly lighter and more vibrant than the list background. Utilizing the `SecondaryContainer` dynamic token and a 6dp shadow elevation, the pill now has a powerful 3D protruding effect.
*   **Precision Progress Tracking**: 
    *   Optimized the progress indicator to a sleek 48dp width with rounded 4dp edges, centered perfectly within the status pillar.
    *   Removed hardcoded white text, transitioning to the lightest Material 3 dynamic tokens for superior accessibility and theme integration.
*   **Unified Status Pillar**: Reimagined the due days indicator and progress bar into a single, beautiful component with integrated progress and centered typography.
*   **Precision UI Alignment**: Standardized the subscription list layout. Urgency indicators are perfectly right-aligned in a clean, vertical column.
*   **Intelligent Urgency Tracking**: The progress bar only activates when the due date is within 5 days.
*   **Intuitive Payment Editing**: Removed the dedicated edit icon in the payment history. Now, the entire row is clickable with a subtle ripple effect.
*   **Unified Visual Language**: Standardized the destructive action styling. The payment deletion icon now matches the subscription deletion icon's color (Error token).
*   **Fluid Motion & Transitions**: Integrated Material 3 Expressive alpha transitions for smooth, non-stuttering fade animations.
*   **Refined Typography**: Removed aggressive "hard" bolding throughout the app to align with the cleaner design language of Android 16 QPR 3.
*   **Immersive Haptic Feedback**: Subtle, tactile haptics integrated for scrolling, modal expansion, and all meaningful interactions.
*   **Expanded Categories**: Added Loan, Software, Family, Investment, and Insurance categories.

## 🚀 Key Features

*   **Subscription Tracking**: Effortlessly manage costs, recurrence, and due dates.
*   **Daily Reminders**: Integrated `WorkManager` notifications for upcoming payments.
*   **Usage-Based Personalization**: The app learns your preferences, bubbling up your most used choices.
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
