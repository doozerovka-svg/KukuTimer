# 🍚 Kuku Timer (ククターマー)

> **"A chance for salvation while the pot of rice comes to a boil."**  
> *«Шанс на спасение, пока закипает котел с рисом»*

**Kuku Timer** is an intentional digital detox & mindful focus Android application. Inspired by traditional Japanese folklore and Buddhist parables of delayed gratification, it introduces a mindful friction barrier before opening distracting or habitual mobile applications.

---

## 🍵 Philosophy & Psychology

In classical Zen and folklore narratives, an individual is often granted a brief pause—a moment of contemplation and salvation—measured by the time it takes for a pot of rice to cook.

Traditional app blockers either:
- Block apps completely (leading to frustration and eventual uninstallation), or
- Set strict daily quotas (which fail to address impulsive urges in the moment).

**Kuku Timer** takes a different psychological approach:
1. **The 10-Minute Cook Time:** When you attempt to open a restricted app (e.g., TikTok, Instagram, games), Kuku Timer intercepts the launch and begins a 10-minute waiting period.
2. **Mindful Detachment:** You are encouraged to put your phone down, go about your day, or reflect while the "rice cooks".
3. **The 2-Minute Window of Opportunity:** Once the 10 minutes expire, a 2-minute access window opens with a notification.
   - **If you enter the app within 2 minutes:** You have proven intentionality and may use the app freely for that session.
   - **If you miss the window:** The opportunity passes ("the rice cools down"), the state resets, and any subsequent attempt requires a fresh 10-minute wait.
4. **Self-Restriction:** You can even restrict the Kuku Timer application itself, preventing impulsive disablement of your focus rules.

---

## ✨ Features

- 📱 **App Selector:** Choose any installed application on your device to place under Kuku Timer restriction.
- ⏳ **Intelligent Interception:** Background monitoring service detects when a restricted app is launched and smoothly brings up the Zen timer screen.
- 🔔 **Mindful Notifications:** Exact alarms notify you as soon as your 2-minute entry window opens.
- 🌙 **Session Lifecycle:** Automatic session reset upon screen lock (`ACTION_SCREEN_OFF`), ensuring you remain mindful each time you pick up your phone.
- 🎨 **Modern Android Tech Stack:** Built with Jetpack Compose, Kotlin Coroutines, DataStore Preferences, and Material 3 design.

---

## 🛠 Tech Stack & Architecture

- **Language:** Kotlin 2.x
- **UI Framework:** Jetpack Compose & Material 3
- **Storage:** Jetpack DataStore (Preferences)
- **Background Engine:** Android Foreground Service with `UsageStatsManager` polling
- **Timers & Notifications:** `AlarmManager` (Exact Alarms) & `NotificationManager`
- **Build System:** Gradle (Kotlin DSL, Version Catalogs `libs.versions.toml`)

---

## 🚀 Getting Started & Permissions

### Prerequisites
- Android Studio Ladybug / Meerkat or newer (or Android CLI tools)
- JDK 17+
- Android SDK 36 (minSdk 24)

### Building the Project
```bash
# Clone the repository
git clone https://github.com/doozerovka-svg/KukuTimer.git
cd KukuTimer

# Build Debug APK
./gradlew assembleDebug
```

### Required Permissions
To function effectively as a digital wellbeing tool, the app requires:
1. **Usage Access Permission (`PACKAGE_USAGE_STATS`):** Required to detect when a restricted application is brought to the foreground.
2. **Display over other apps (`SYSTEM_ALERT_WINDOW`):** To present the timer screen over target apps.
3. **Post Notifications (`POST_NOTIFICATIONS`):** To alert you when your 2-minute access window opens.
4. **Exact Alarms (`SCHEDULE_EXACT_ALARM`):** For timely delivery of notifications.

---

## 📜 License

Distributed under the MIT License.
