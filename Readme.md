# Локальный поисковый движок

## Описание
Этот проект — поисковый движок на Spring Boot и MySQL, который индексирует сайты и ищет информацию.

## Как запустить?
1. Установите JDK 17, MySQL, Maven.
2. Склонируйте репозиторий:

git clone https://github.com/username/search-engine.git

3. Настройте application.yaml (укажите доступ к БД).
4. Запустите проект:

mvn spring-boot:run
5. Проверьте работу через API.



##  API:
GET http://localhost:8080/api/search?query=java

- GET /api/startIndexing – запустить индексацию
- GET /api/stopIndexing – остановить индексацию
- POST /api/indexPage?url={URL} – индексировать одну страницу
- GET /api/search?query={search_text} – поиск
- GET /api/statistics – статистика индексации

## Примеры API:
Ответ:
`json
{
"result": true,
"count": 1,
"data": [
{
"site": "https://www.skillbox.ru",
"siteName": "Skillbox",
"uri": "/course/java-developer",
"title": "Курс Java Developer",
"snippet": "Этот <b>курс</b> поможет вам стать разработчиком",
"relevance": 0.95
}
]
}