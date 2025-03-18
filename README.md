Метод вывешен на эндпоинт /titles (POST). JSON для тела запроса принимается в виде:
```
{
    "urls": [
        "url1",
        "url2",
        "url3"
        ]
}
```
Пример:
```
{
    "urls": [
        "https://www.google.com/",
        "github.com/",
        "http://invalid.url"
        ]
}
```
В случае, если у URL отсутствует префикс http:// или https://, к нему будет автоматически добавлен http:// для попытки подключения к сайту.
Готовый curl для тестирования (при локальном развёртывании на порту 9000):
```
curl -X POST http://localhost:9000/titles \
  -H "Content-Type: application/json" \
  -d '{"urls": ["https://www.google.com/", "github.com/", "http://invalid.url"]}'
```
