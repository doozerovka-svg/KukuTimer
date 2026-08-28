# ☁️ Сборка и публикация iOS приложения без Mac (через GitHub Actions)

Этот документ описывает, как с любого компьютера на **Windows** автоматически компилировать, тестировать и публиковать приложение **Kuku Timer для iOS** в **App Store / TestFlight** с помощью бесплатных облачных виртуальных машин Apple Mac от GitHub.

---

## 🎯 Как это работает

1. Вы пишете или обновляете код на Windows и делаете `git push` в репозиторий.
2. **GitHub Actions** запускает сервер на **macOS 14 (Apple Silicon M1/M2)** с установленным **Xcode 15+**.
3. Сервер автоматически устанавливает `xcodegen`, генерирует проект `.xcodeproj`, собирает приложение со всеми тремя App Extensions (`ShieldConfig`, `ShieldAction`, `DeviceActivityMonitor`).
4. Приложение компилируется в `.ipa` и загружается в ваш личный кабинет **App Store Connect** / **TestFlight** для тестирования на вашем iPhone.

---

## 📋 Инструкция по первоначальной настройке

### Шаг 1. Получение Apple Developer Account
1. Зарегистрируйтесь на сайте [developer.apple.com](https://developer.apple.com) и оплатите подписку разработчика ($99/год).
2. Запросите entitlement `Family Controls` в личном кабинете Apple Developer (раздел *Certificates, Identifiers & Profiles* -> *Identifiers* -> выберите Bundle ID `com.doozerovka.kukutimer` -> включите **Family Controls**).

### Шаг 2. Создание ключа API App Store Connect
1. Зайдите в [App Store Connect](https://appstoreconnect.apple.com) -> вкладка **Users and Access** -> **Integrations** -> **App Store Connect API**.
2. Нажмите **+** (Создать ключ):
   - Имя: `GitHub Actions CI`
   - Access: `Admin` или `App Manager`
3. Скачайте файл ключа `.p8` (скачать можно только один раз!).
4. Скопируйте:
   - **Key ID** (например, `2X9R4HXF34`)
   - **Issuer ID** (например, `57246542-96fe-1a63-e053-0824d011072a`)

### Шаг 3. Добавление секретов в GitHub Репозиторий
Зайдите в ваш репозиторий на GitHub:
**Settings** -> **Secrets and variables** -> **Actions** -> **New repository secret**:

| Имя секрета | Описание |
| :--- | :--- |
| `APP_STORE_CONNECT_KEY_ID` | Ваш Key ID из шага 2 |
| `APP_STORE_CONNECT_ISSUER_ID` | Ваш Issuer ID из шага 2 |
| `APP_STORE_CONNECT_API_KEY_BASE64` | Содержимое файла `.p8` в base64 кодировке |
| `APPLE_CERTIFICATE_BASE64` | Сертификат Apple Distribution (`.p12`) в base64 |
| `APPLE_CERTIFICATE_PASSWORD` | Пароль от файла `.p12` |
| `PROVISIONING_PROFILE_BASE64` | Файл профиля `.mobileprovision` в base64 |

> 💡 **Как быстро перевести файл в base64 на Windows (через PowerShell):**
> ```powershell
> [Convert]::ToBase64String([IO.File]::ReadAllBytes("AuthKey_XXXXXX.p8")) | Set-Clipboard
> ```
> Значение сразу скопируется в буфер обмена!

---

## 🚀 Как запустить сборку с Windows прямо в браузере

1. Откройте ваш репозиторий на GitHub.
2. Перейдите во вкладку **Actions** вверху.
3. В левой колонке выберите **Build & Release iOS**.
4. Нажмите кнопку справа **Run workflow**:
   - Выберите ветку `main`.
   - Поставьте галочку `Upload build to App Store Connect / TestFlight` (если хотите сразу отправить на iPhone).
   - Нажмите зеленую кнопку **Run workflow**.
5. Через 3–5 минут сборка завершится, и приложение появится у вас в приложении **TestFlight** на айфоне!
