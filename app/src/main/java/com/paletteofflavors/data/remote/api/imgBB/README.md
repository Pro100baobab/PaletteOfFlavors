
# API ImgBB

**Версия API:** 1

API v1 сервиса ImgBB позволяет загружать изображения на хостинг.

## Метод запроса

Вызовы API v1 можно выполнять с помощью методов `POST` или `GET`.  
**Рекомендуется использовать `POST`**, так как запросы `GET` ограничены максимальной допустимой длиной URL.

## Загрузка изображения

```
POST https://api.imgbb.com/1/upload
```

### Параметры

| Параметр      | Обязательный | Описание |
|---------------|:------------:|-----------|
| `key`         | ✅           | Ключ API. |
| `image`       | ✅           | Двоичный файл, данные в формате Base64 или URL изображения (максимум 32 MB). |
| `name`        | ❌           | Имя файла. Определяется автоматически при загрузке через `multipart/form-data`. |
| `expiration`  | ❌           | Время жизни загруженного файла в секундах (от 60 до 15 552 000). По истечении срока изображение автоматически удаляется. |

### Пример вызова (cURL)

```bash
curl --location --request POST "https://api.imgbb.com/1/upload?expiration=600&key=YOUR_CLIENT_API_KEY" \
  --form "image=R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7"
```

> **Примечание:** при загрузке локальных файлов всегда используйте `POST`. Кодирование URL может изменить исходные данные Base64 или привести к превышению лимита длины URL при использовании `GET`.

## Ответ API

Ответы API v1 возвращают информацию о загруженном изображении в формате JSON.  
Заголовки ответа содержат код состояния HTTP, а тело ответа включает свойство `status` для удобной проверки успешности запроса.

### Пример успешного ответа (JSON)

```json
{
  "data": {
    "id": "2ndCYJK",
    "title": "c1f64245afb2",
    "url_viewer": "https://ibb.co/2ndCYJK",
    "url": "https://i.ibb.co/w04Prt6/c1f64245afb2.gif",
    "display_url": "https://i.ibb.co/98W13PY/c1f64245afb2.gif",
    "width": "1",
    "height": "1",
    "size": "42",
    "time": "1552042565",
    "expiration": "0",
    "image": {
      "filename": "c1f64245afb2.gif",
      "name": "c1f64245afb2",
      "mime": "image/gif",
      "extension": "gif",
      "url": "https://i.ibb.co/w04Prt6/c1f64245afb2.gif"
    },
    "thumb": {
      "filename": "c1f64245afb2.gif",
      "name": "c1f64245afb2",
      "mime": "image/gif",
      "extension": "gif",
      "url": "https://i.ibb.co/2ndCYJK/c1f64245afb2.gif"
    },
    "medium": {
      "filename": "c1f64245afb2.gif",
      "name": "c1f64245afb2",
      "mime": "image/gif",
      "extension": "gif",
      "url": "https://i.ibb.co/98W13PY/c1f64245afb2.gif"
    },
    "delete_url": "https://ibb.co/2ndCYJK/670a7e48ddcb85ac340c717a41047e5c"
  },
  "success": true,
  "status": 200
}
```