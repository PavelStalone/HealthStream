# 🩺 HealthStream

**HealthStream** — это современное Android-приложение для мониторинга показателей здоровья, разработанное с упором на приватность и удобство анализа данных.

> [!IMPORTANT]
> **Главная фишка приложения** — генерация удобных и детальных отчетов. Вы можете мгновенно передать накопленные данные своему врачу или доверенному лицу напрямую, удобным вам способом.

---

## 📺 Демонстрация работы

### Процесс работы приложения
Здесь будет представлено видео процесса использования.
* **Слева**: Процесс онбординга и мгновенной генерации отчета.
* **Справа**: Просмотр текущих показаний и графиков внутри приложения.

<!-- PLACEHOLDER_FOR_VIDEO -->
| Онбординг и отчет | Мониторинг данных |
| :--- | :--- |
| ![Video Placeholder Left](https://via.placeholder.com/300x600?text=Onboarding+Video) | ![Video Placeholder Right](https://via.placeholder.com/300x600?text=Monitoring+Video) |

### Детальные отчеты
Примеры того, как выглядит готовый отчет, который получает ваш врач.

<!-- PLACEHOLDER_FOR_REPORT_IMAGES -->
| Структура отчета | Визуализация данных |
| :--- | :--- |
| ![Report Structure](https://via.placeholder.com/300x400?text=Report+Structure) | ![Report View](https://via.placeholder.com/300x400?text=Report+Visuals) |

---

## 🚀 Основные возможности

*   📈 **Мониторинг Vitals**: Отслеживание давления, пульса, сатурации и других важных метрик.
*   📄 **Автономные отчеты**: Создание PDF/CSV отчетов локально на устройстве.
*   🔗 **Google Health Connect**: Полная синхронизация с экосистемой Android Health.
*   📡 **BLE Интеграция**: Поддержка внешних Bluetooth Low Energy устройств для точных измерений.
*   🎨 **Material 3**: Современный и адаптивный интерфейс на Jetpack Compose.
*   🛡️ **Privacy First**: Ваши данные принадлежат только вам и хранятся локально.

---

## 🛠 Технологический стек

Приложение построено на современных технологиях Android-разработки:

| Категория | Технологии |
| :--- | :--- |
| **Язык** | Kotlin |
| **UI** | Jetpack Compose, Material 3 |
| **Архитектура** | Clean Architecture, Multi-module (API/Impl) |
| **DI** | Hilt |
| **Навигация** | Navigation3 + Nav3Router |
| **Data** | Room, DataStore, Health Connect |
| **Связь** | Bluetooth Low Energy (BLE) |
| **Асинхронность** | Coroutines & Flow |
| **Сборка** | Gradle Kotlin DSL, Convention Plugins, Version Catalogs |

---

## 📡 BLE Интеграция

Приложение использует удобный алгоритм поиска и идентификации медицинских устройств. 

### Схема работы BLE
Ниже представлена схема того, как приложение сканирует эфир, фильтрует устройства и определяет их тип для получения данных.

<!-- PLACEHOLDER_FOR_BLE_DIAGRAM -->
![BLE Scanning Diagram](https://via.placeholder.com/800x400?text=BLE+Scanning+and+Device+Identification+Scheme)

---

## 🏗 Архитектура модулей

Проект разделен на логические слои для обеспечения масштабируемости и тестируемости:

*   **`:app`** — Точка входа, конфигурация Hilt и навигационный граф.
*   **`:feature`** — Изолированные фичи: `home`, `user`, `report`, `measurement`, `onboarding`. Используется паттерн `api/impl` для уменьшения времени сборки.
*   **`:data`** — Репозитории и бизнес-логика (`vitals`, `personal`, `setting`).
*   **`:source`** — Реализация источников данных:
    *   `remote:ble` — Работа с Bluetooth устройствами.
    *   `local:room` — Локальная база данных.
    *   `local:healthconnect` — Интеграция с Google Health.
*   **`:core`** — Общие UI компоненты, дизайн-система, графики и навигационные утилиты.

---

## ⚙️ Установка и сборка

### 1. Клонирование репозитория
```bash
git clone https://github.com/PavelStalone/HealthStream.git
```

### 2. Сборка проекта

```bash
./gradlew :app:assembleDebug
```
После завершения сборки APK файл будет доступен в `app/build/outputs/apk/debug/`.

---

Разработано с ❤️ для вашего здоровья.
