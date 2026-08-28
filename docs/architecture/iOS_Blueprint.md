# Kuku Timer: iOS Architecture & Porting Blueprint

## 1. Product Overview (iOS Context)

The iOS version of Kuku Timer aims to replicate the core philosophy of the Android app: introducing a mindful friction barrier (10-minute wait, 2-minute entry window) before allowing access to distracting applications.

Due to strict iOS sandboxing, the technical implementation differs entirely from Android. We will use the **iOS Screen Time API** (`FamilyControls`, `ManagedSettings`, `DeviceActivity`) introduced in iOS 15.

## 2. Core Architectural Differences: iOS vs Android

| Feature | Android (Current) | iOS (Planned) |
| :--- | :--- | :--- |
| **App Selection** | Custom list of installed apps via `PackageManager`. | System-provided `FamilyActivityPicker`. We receive opaque tokens, not app names. |
| **Interception** | Polling `UsageStatsManager` in a Foreground Service. | System-level interception. iOS automatically blocks the app. |
| **UI Overlay** | Custom UI drawn over the app using `SYSTEM_ALERT_WINDOW`. | Customizing the system lock screen via `ShieldConfiguration` App Extension. |
| **User Action** | Interacting with the custom overlay. | Tapping buttons on the system Shield via `ShieldActionDelegate` Extension. |
| **Session Reset** | Listening to `ACTION_SCREEN_OFF` broadcast. | Fixed time-window expirations (iOS does not broadcast screen locks to apps). |

## 3. iOS System Architecture & Modules

The Xcode project will consist of the main application and several App Extensions communicating via **App Groups** (shared `UserDefaults`).

### 3.1 Main App Target (SwiftUI)
- **Onboarding & Permissions:** Requests `FamilyControls` authorization.
- **Dashboard:** Displays statistics (if applicable) and a button to open the `FamilyActivityPicker`.
- **State Manager:** Checks the timer state when opened via Notification to unshield apps for 2 minutes.

### 3.2 Shield Configuration Extension
- An iOS App Extension responsible for drawing the UI when a user opens a restricted app.
- **Customization:** We will provide the Kuku Timer branding (Rice bowl icon, "The rice is cooking" text).
- **Dynamic UI:** The UI will reflect the current state (e.g., "Start cooking" vs "Cooking in progress: 8 mins left").

### 3.3 Shield Action Extension
- Handles button presses on the Shield screen.
- When the user taps "Primary Button" (Start Timer):
  - Saves the timestamp to shared `UserDefaults`.
  - Schedules a Local Notification (`UNNotificationRequest`) for 10 minutes in the future.
  - Leaves the shield active.

## 4. User Flow & State Machine

1. **Idle / Restricted State**
   - The user selects apps to restrict.
   - The main app sets `ManagedSettingsStore().shield.applications` to the selected tokens.
   - Target apps are now blocked.

2. **Timer Initiation (The 10-Minute Cook)**
   - User taps a restricted app (e.g., Instagram).
   - iOS displays our customized Shield.
   - User taps "Cook Rice" (Shield Action).
   - The Action Extension schedules a Local Notification for +10 minutes.
   - The user must leave the app and wait.

3. **The Window of Opportunity (2-Minute Access)**
   - 10 minutes pass. The Local Notification fires: *"The rice is ready! You have 2 minutes to eat."*
   - User taps the notification. This deep-links into the Kuku Timer main app.
   - **Validation:** The main app verifies that exactly ~10 minutes have passed.
   - **Unshield:** The main app clears the shield: `ManagedSettingsStore().shield.applications = nil`.
   - **Session Expiry:** The main app immediately schedules a background task/timer for 2 minutes. After 2 minutes, it re-applies the shield restrictions.

4. **Missed Window**
   - If the user ignores the notification and tries to open the app 15 minutes later, the main app's state machine will recognize the window has passed, reset the state, and the user must start the 10-minute timer again.

## 5. Task Decomposition & Implementation Phases

### Phase 1: Project Setup & Entitlements
- [ ] Create a new Xcode project (`KukuTimeriOS`) with SwiftUI.
- [ ] Set up App Groups (`group.com.doozerovka.kukutimer`) to share data between the main app and extensions.
- [ ] Request and apply the `FamilyControls` entitlement via the Apple Developer Portal.
- [ ] Implement request authorization for `AuthorizationCenter.shared.requestAuthorization`.

### Phase 2: App Selection & Screen Time Auth
- [ ] Implement `FamilyActivityPicker` in a SwiftUI view.
- [ ] Save the user's `FamilyActivitySelection` (opaque tokens) to App Group `UserDefaults`.
- [ ] Implement `ManagedSettingsStore` logic to apply shields to the selected tokens.

### Phase 3: Shield UI & Action Extensions
- [ ] Add a **Shield Configuration Extension** target to the Xcode project.
- [ ] Design the Shield UI to match Kuku Timer's zen aesthetic (Icon, Title, Subtitle, Primary Button).
- [ ] Add a **Shield Action Extension** target.
- [ ] Implement the `handle(action: .primaryButtonPressed)` logic to initiate the timer.

### Phase 4: State Management & Notifications
- [ ] Request Notification permissions (`UNUserNotificationCenter`).
- [ ] Implement logic in the Shield Action Extension to schedule a notification +10 minutes from the button press.
- [ ] Write the state machine in the Main App:
  - Read `startTime` from App Groups.
  - Determine if the user is in the "2-minute window".
  - Temporarily remove shields if they are.
  - Schedule a re-shield operation after 2 minutes.

### Phase 5: Polish & UI
- [ ] Create the Main App Dashboard (Jetpack Compose -> SwiftUI translation).
- [ ] Add haptics, localization, and accessibility support.
- [ ] Test edge cases (device reboot, timezone changes, notification clearing).

## 6. Out of Scope
- Perfect `ACTION_SCREEN_OFF` emulation. We will rely on strict 2-minute unshield windows instead of tracking device locks.
- Granular analytics of which apps were opened (iOS Screen Time API prevents knowing *which* app was blocked for privacy reasons).
