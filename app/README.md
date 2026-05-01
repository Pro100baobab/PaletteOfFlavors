# Оптимизация архитектуры и рефакторинг мобильного приложения «Палитра вкусов»

Ветка: `feature/unify-recipe-model`

## Цель изменений

1. Унифицировать представление рецептов во всех слоях приложения – теперь используется единая модель `NetworkRecipe` (domain-слой), не зависящая от конкретных таблиц локальной БД.
2. Упростить локальное хранение: оставлены только таблицы `cashRecipes` (кэш сетевых рецептов) и `savedRecipes` (избранное). Пользовательские рецепты временно вынесены на серверную часть.
3. Повысить стабильность сетевого слоя: замена прямого подключения к Turso (libsql) на HTTP‑запросы к Turso Pipeline API.
4. Добавить поддержку приватности рецептов (`isPublic`), а также списков пользователей, лайкнувших и сохранивших рецепт.

---

## Изменения серверной БД (Turso)

В таблицу `Recipes` добавлены три новых столбца:

```sql
ALTER TABLE Recipes ADD COLUMN isPublic INTEGER NOT NULL DEFAULT 1 CHECK (isPublic IN (0, 1));
ALTER TABLE Recipes ADD COLUMN liked_list TEXT;   -- JSON-массив id пользователей, например "[1,2,3]"
ALTER TABLE Recipes ADD COLUMN saved_list TEXT;   -- JSON-массив id пользователей
```

* `isPublic` – флаг публичного рецепта (0 = приватный, 1 = публичный).
* `liked_list`, `saved_list` – хранят идентификаторы пользователей в виде JSON‑массива.

---

## Изменения в коде

### 1. Единая модель рецепта

- **Создан** `NetworkRecipe` (`data/local/database/model/NetworkRecipe.kt`) — data-класс, не являющийся Room‑сущностью.  
  Содержит все поля, включая новые: `isPublic`, `likedListOfUsers`, `savedListOfUsers`.
- **Удалены** устаревшие классы:
    - `Recipe` (старая таблица `resipes`)
    - `SavedRecipe` (старая версия таблицы `savedRecipes`)
    - `NetworkRecipe` как Room‑entity (заменён на `CachedRecipeEntity` / `SavedRecipeEntity`).

### 2. Room‑сущности локальной БД

- **Добавлены** две новые entity для локальных таблиц:
    - `CachedRecipeEntity` (таблица `cashRecipes`)
    - `SavedRecipeEntity` (таблица `savedRecipes`)
- Оба класса полностью повторяют поля `NetworkRecipe`, включая `isPublic`, `likedListOfUsers`, `savedListOfUsers`.
- Старая таблица `resipes` удалена.

### 3. Конвертеры типов

- **Доработан** `Converters.kt` – добавлены методы для сериализации/десериализации `List<Int>`.
- **Удалён** `RecipesConverters.kt` (ручные мапперы `toSavedRecipe()` / `toNetworkRecipe()` больше не нужны).

### 4. DAO‑интерфейсы

- `CashDao` теперь работает с `CachedRecipeEntity`.
- `SavedRecipeDao` – с `SavedRecipeEntity`.
- `RecipeDao` временно удалён (функциональность пользовательских рецептов вынесена на сервер).

### 5. База данных и миграция

- Версия `AppDatabase` повышена до **4**.
- Добавлена миграция `MIGRATION_3_4`:
    - Удаляет таблицу `resipes`.
    - Пересоздаёт `savedRecipes` и `cashRecipes` с новой структурой (включая столбцы `isPublic`, `likedListOfUsers`, `savedListOfUsers`).
    - Переносит существующие данные из старых таблиц (значения новых полей по умолчанию).

### 6. Репозиторий рецептов

- `RecipeRepository` полностью переписан:
    - Работает только с `savedRecipeDao` и `cashRecipeDao`.
    - Все методы принимают/возвращают `NetworkRecipe`; преобразование в/из Entity происходит внутри репозитория с помощью приватных мапперов.
    - Удалены устаревшие методы для `RecipeDao`.

### 7. ViewModel и фабрики

- **`FavoritesViewModel`**
    - Поля `myRecipes` удалены.
    - `savedRecipes` и `cashedRecipes` теперь возвращают `Flow<List<NetworkRecipe>>`.
    - Методы `addSavedRecipe`, `deleteSavedRecipe`, `addCashedRecipe` принимают `NetworkRecipe`.

- **`CreateRecipeViewModel`**
    - Добавлено поле `isPublic` и метод `setIsPublic()`.
    - Метод `buildRecipe()` формирует готовый объект `NetworkRecipe` (без сохранения в локальную БД).
    - Фабрика `CreateRecipeViewModelFactory` больше не требует `RecipeDao`.

- **`RecipeSharedViewModel`**
    - Хранит только один выбранный рецепт (`selectedRecipe: StateFlow<NetworkRecipe?>`).
    - Удалены отдельные потоки для старых типов рецептов.

### 8. UI‑фрагменты и адаптеры

- **`RecipeAdapter` и `RecipeDetailsFragment` удалены**.
- **`NetworkRecipeAdapter`** продолжает работать с `NetworkRecipe` без изменений.
- **`NetworkRecipeDetailsFragment`**
    - Избавлен от жёсткой привязки к источнику (ранее требовал параметр `fragment`).
    - Всегда берёт данные из `sharedViewModel.selectedRecipe`.
- **`FavoritesFragment`**
    - Оставлена только вкладка «Избранное»; собственная вкладка убрана.
    - Все операции выполняются напрямую с `NetworkRecipe`.
- **`FridgeFragment` и `SearchFragment`**
    - Убраны вызовы устаревших конвертеров `.toSavedRecipe()`; везде используется `NetworkRecipe`.
- **`CreateRecipeFragment`**
    - В макет добавлен чекбокс «Публичный рецепт» (`isPublicCheckbox`).
    - Кнопка «Сохранить» вызывает `buildRecipe()` и (в заглушке) отправляет рецепт на сервер.

### 9. Сетевой слой: переход на HTTP‑запросы

Класс `Turso` доработан для повышения стабильности и надёжности.

- **Заменён** метод `getAllNetworkRecipesFlow()` на приостанавливаемую функцию `getAllNetworkRecipes()`, возвращающую `List<NetworkRecipe>`.
- Вместо прямого использования libsql выполняется HTTP‑POST запрос к **Turso Pipeline API** (`https://{host}/v2/pipeline`).
- Тело запроса формируется в формате JSON с командами `execute` и `close`.
- Ответ парсится вручную из структуры `results[0].response.result`:
    - Извлекаются названия колонок.
    - Для каждой строки создаётся объект `NetworkRecipe`, включая новые поля (`isPublic`, `likedListOfUsers`, `savedListOfUsers`).
- Добавлен вспомогательный метод `parseJsonIntList()` для безопасного парсинга JSON‑массивов.
- Ингредиенты (`ingredients`) временно возвращаются пустым списком; предполагается их получение отдельным запросом.
- Сохранён fallback‑режим: при отсутствии интернета используется локальный кэш.

### 10. Прочие изменения

- В `MainActivity` обновлена инициализация репозитория и ViewModel согласно новой архитектуре.
- В `ProfileFragment` временно скрыто отображение количества собственных рецептов (заглушка).
- Из всех фрагментов удалены импорты и ссылки на удалённые классы.

---

## Структура ответа Turso HTTP API (Pipeline)

При использовании HTTP‑метода `POST` на эндпоинт `https://<your-db>.turso.io/v2/pipeline`  
тело запроса должно содержать JSON вида:

```json
{
  "requests": [
    { 
      "type": "execute", 
      "stmt": { "sql": "SELECT * FROM Recipes" } 
    },
    { "type": "close" }
  ]
}
```

Ответ сервера имеет следующую структуру (успешный ответ, `200 OK`):

```jsonc
{
  "baton": null,            // внутренний идентификатор (можно игнорировать)
  "base_url": null,         // базовый URL (можно игнорировать)
  "results": [              // массив результатов каждого запроса из "requests"
    {
      "type": "ok",         // результат первого запроса ("execute")
      "response": {
        "type": "execute",  // тип результата
        "result": {
          "cols": [         // описание колонок
            { "name": "title", "decltype": "TEXT" },
            { "name": "cookTime", "decltype": "INTEGER" }
            // ... остальные колонки
          ],
          "rows": [         // массив строк с данными
            [               // каждая строка — массив ячеек
              { "type": "text", "value": "Спагетти Карбонара" },
              { "type": "integer", "value": "25" }
              // ... ячейки в порядке колонок
            ],
            [               // следующая строка
              { "type": "text", "value": "Омлет с овощами" },
              { "type": "integer", "value": "10" }
            ]
          ]
        }
      }
    },
    {
      "type": "ok",         // результат второго запроса ("close")
      "response": {
        "type": "close"     // подтверждение закрытия
      }
    }
  ]
}
```

### Пояснения

- **`results`** содержит ровно столько элементов, сколько было отправлено запросов в `"requests"`.
- Каждый элемент `results` имеет поле `type`:
  - `"ok"` – запрос выполнен успешно;
  - `"error"` – произошла ошибка (содержит описание ошибки).
- Для запросов типа `"execute"` внутри `response` находится объект `result` с полями:
  - `cols` – массив определений колонок (`name`, `decltype`).
  - `rows` – массив строк; каждая строка – массив ячеек.  
    Ячейка – объект с полем `type` (тип данных: `"text"`, `"integer"` и др.)  
    и полем `value` в виде строки.
- Запрос `"close"` освобождает ресурсы соединения на сервере; его обязательно нужно отправлять последним.

### Пример кода для разбора (Android/Kotlin)

```kotlin
val okResult = resultsArray.getJSONObject(0)
if (okResult.getString("type") != "ok") { /* обработка ошибки */ }

val resultObj = okResult
    .getJSONObject("response")
    .getJSONObject("result")

val columns = resultObj.getJSONArray("cols")
val rows = resultObj.getJSONArray("rows")

// Карта: имя колонки -> индекс
val colIndex = mutableMapOf<String, Int>()
for (i in 0 until columns.length()) {
    val col = columns.getJSONObject(i)
    colIndex[col.getString("name")] = i
}

// Разбор строки
for (i in 0 until rows.length()) {
    val row = rows.getJSONArray(i)
    val title = row.getJSONObject(colIndex["title"]!!).getString("value")
    // ...
}
```
---

## Итог

Все рецепты в приложении теперь представлены единообразно, локальная БД упрощена, сетевой слой стал стабильнее и легче поддерживается.  
Код проекта проходит компиляцию без ошибок, существующий функционал сохранения/отображения избранных и кэшированных рецептов полностью сохранён.

```