# 🍚 Kuku Timer for iOS (ククターマー)

> **"A chance for salvation while the pot of rice comes to a boil."**  
> *«Шанс на спасение, пока закипает котел с рисом»*

Нативная реализация **Kuku Timer** для **iOS 16+**, использующая **SwiftUI** и **Apple Screen Time API** (`FamilyControls`, `ManagedSettings`, `ManagedSettingsUI`, `DeviceActivity`).

---

## 🍵 Архитектура и Структура Проекта

Проект состоит из основного приложения и трех системных App Extensions, взаимодействующих через общую группу **App Group** (`group.com.doozerovka.kukutimer`):

```
ios/
├── project.yml                          # Конфигурация генератора проекта XcodeGen
├── KukuTimer/                           # Основное приложение (Main App Target)
│   ├── KukuTimerApp.swift               # Точка входа, авторизация FamilyControls и Notifications
│   ├── Info.plist                       # Манифест приложения
│   ├── KukuTimer.entitlements           # App Group и FamilyControls разрешения
│   ├── Views/                           # Пользовательский интерфейс (SwiftUI)
│   │   ├── ContentView.swift            # Главный дашборд, статус и выбор приложений
│   │   ├── OnboardingView.swift         # Пошаговая настройка разрешений
│   │   ├── TutorialView.swift           # 4 слайда философии Дзен и векторная инфографика
│   │   └── ZenTimerView.swift           # Экран таймера с анимацией пара и рисовыми зернами
│   └── Shared/                          # Общие модули (доступны приложению и расширениям)
│       ├── TimerState.swift             # Менеджер состояний, тайминги, Screen Time Store
│       ├── Theme.swift                  # Японская цветовая палитра (Kinari, Sumi, Shu-iro, Kinpaku)
│       └── ZenComponents.swift          # Векторные компоненты (Bonsai, Rice Bowl, RiceGrainDial)
├── ShieldConfig/                        # Расширение UI экрана блокировки (Shield UI)
│   ├── ShieldConfigurationExtension.swift # Динамическая отрисовка экрана ожидания
│   ├── Info.plist
│   └── ShieldConfig.entitlements
├── ShieldAction/                        # Расширение обработки нажатий кнопок блокировки
│   ├── ShieldActionExtension.swift      # Запуск 10-минутного таймера и разблокировка на 2 мин
│   ├── Info.plist
│   └── ShieldAction.entitlements
└── DeviceActivityMonitor/               # Фоновый мониторинг интервалов и активности
    ├── DeviceActivityMonitorExtension.swift
    ├── Info.plist
    └── DeviceActivityMonitor.entitlements
```

---

## 🚀 Сборка и Запуск Проекта

### Сборка без Mac (с Windows через GitHub Actions) ☁️
Если у вас нет физического Mac, вы можете собирать и публиковать приложение прямо с Windows через бесплатные виртуальные машины GitHub! 
👉 Подробная инструкция: **[Сборка iOS на Windows через GitHub Actions](../docs/architecture/iOS_CICD_Setup.md)**.

---

### Локальная сборка (если есть Mac) 🍏
- Mac с установленной **macOS Sonoma / Sequoia**
- **Xcode 15.0+**
- **XcodeGen** (утилита для генерации `.xcodeproj` из `project.yml`)
- Реальное устройство с **iOS 16.0+**
- Аккаунт **Apple Developer** (для получения entitlement `com.apple.developer.family-controls`)

### 2. Установка XcodeGen (на Mac)
```bash
brew install xcodegen
```

### 3. Генерация проекта Xcode
Перейдите в директорию `ios` и сгенерируйте `.xcodeproj`:
```bash
cd ios
xcodegen generate
```
После выполнения команды появится готовый файл **`KukuTimer.xcodeproj`**.

### 4. Настройка подписи в Xcode
1. Откройте `KukuTimer.xcodeproj` в Xcode.
2. В настройках каждого Target (`KukuTimer`, `ShieldConfig`, `ShieldAction`, `DeviceActivityMonitor`) во вкладке **Signing & Capabilities**:
   - Выберите вашу команду разработчика (**Development Team**).
   - Убедитесь, что capability **App Groups** активна с идентификатором `group.com.doozerovka.kukutimer`.
   - Для основного таргета включена capability **Family Controls**.
3. Выберите подключенный iPhone и нажмите **Cmd + R** (Run).

---

## 🧘‍♂️ Как Работает Приложение на iOS

1. **Выбор приложений:** В основном приложении через системный `FamilyActivityPicker` выбираются блокируемые приложения.
2. **Перехват:** При попытке открыть выбранное приложение (например, Instagram), iOS автоматически отображает кастомный темный экран в стиле дзен (`ShieldConfigurationExtension`).
3. **10-минутная пауза:** Пользователь нажимает кнопку *"Поставить рис вариться"*. Запускается таймер, планируется точное Push-уведомление через 10 минут, экран остается заблокированным.
4. **2-минутное окно возможностей:** Ровно через 10 минут приходит уведомление: *"Котел закипел. Рис готов!"*.
5. **Вход:** При повторном открытии приложения в течение 2 минут кнопка на экране меняется на *"🌸 Войти в приложение"*, которая временно снимает Shield и позволяет пользоваться приложением. Если 2 минуты прошли — таймер остывает и сбрасывается.
