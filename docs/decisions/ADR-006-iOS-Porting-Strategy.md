# ADR-006: iOS Porting Strategy

## Status
Accepted

## Date
2026-08-28

## Context
Kuku Timer is currently an Android-only application that uses `UsageStatsManager` and `SYSTEM_ALERT_WINDOW` to intercept app launches and display a timer overlay. We want to port this application to iOS to reach a broader audience.
iOS has strict sandboxing rules and does not allow apps to monitor the usage of other apps or draw over them. However, iOS 15+ introduced the Screen Time API (`FamilyControls`, `ManagedSettings`, `DeviceActivity`), which allows apps to implement parental controls and focus tools.

## Decision
We will build a **Native iOS Application using Swift and SwiftUI**, directly integrating with the Screen Time API. 

## Alternatives Considered

### Kotlin Multiplatform (KMP) + Compose Multiplatform
- **Pros:** Maximum code reuse for business logic and UI (Jetpack Compose is very similar to Compose Multiplatform).
- **Cons:** The core feature of the app relies entirely on platform-specific OS APIs (Screen Time on iOS, Foreground Services on Android). Writing the necessary App Extensions (ShieldConfiguration, ShieldAction) in Kotlin via KMP is highly complex, poorly documented, and prone to breakage.
- **Rejected:** The architectural friction of bridging App Extensions through KMP outweighs the benefits of UI code reuse, especially since the UI is relatively minimal.

### Cross-Platform (Flutter / React Native)
- **Pros:** Single codebase.
- **Cons:** These frameworks cannot natively implement iOS App Extensions without writing native Swift/Objective-C code anyway. We would end up writing the core logic in Swift and only the settings screen in Dart/JS.
- **Rejected:** Unnecessary overhead.

## Consequences
- The iOS app will have a separate codebase from the Android app.
- We will use Swift, SwiftUI, and App Groups (for sharing state between the main app and extensions).
- The user flow will change slightly: instead of a custom overlay appearing *over* the app, users will see the system-provided Shield screen, where they can initiate the timer.
- iOS does not allow perfect detection of "Screen Off" (`ACTION_SCREEN_OFF`). The session reset logic will rely on strict time windows rather than screen state.
- An Apple Developer account is required to request the `FamilyControls` entitlement from Apple.
