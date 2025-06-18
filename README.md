# Palette of Flavors 🍽️

**Приложение для поиска, сохранения и создания рецептов** с возможностью авторизации и синхронизации данных.  
*Разработано с использованием Single Activity-паттерна и архитектуры MVVM , Room, XML и Turso SQLite.*

---

## 📌 Основные функции
- 🔍 **Поиск рецептов** по категориям, ингредиентам или названию.
- ♥️ **Сохранение рецептов** в избранное.
- 🧑‍🍳 **Создание собственных рецептов**.
- 🔄 **Синхронизация** с облачной базой Turso.
- 🔐 **Авторизация** (регистрация, вход, сброс пароля + 2FA через email).

---

## 🛠️ Технологии
- **Архитектура**: MVVM
- **Локальная БД**: Room + SQLite
- **Сетевое взаимодействие**: Turso API + Retrofit (для отправки email через GoogleScript)
- **Навигация**: NavGraph (Navigation API) + транзакции
- **UI**: XML

---

## 🚀 Запуск проекта
1. Склонируйте репозиторий:
   ```bash
   git clone https://github.com/Pro100baobab/PaletteOfFlavors.git
   ```
2. Откройте проект в Android Studio.
3. Запустите через Run 'app'.

---

📂 Структура проекта

com.paletteofflavors/

├── data/                # Данные (Room, API)

├── domain/              # Бизнес-логика (утилиты)

└── presentation/        # UI (Activity, Fragment, ViewModel, Adapter)

---

🤝 Контакты
Telegram: @pro_100_baobab
