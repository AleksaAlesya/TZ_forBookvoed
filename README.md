# tz_for_bookvoed
###Тестовое задание
Разработать небольшой веб-сервис с авторизацией и возможностью управления списком книг.

1. Авторизация по логину и паролю (можно in-memory, без регистрации)
2. Добавлять, редактировать, удалять книги
3. Хранить поля: id, vendorCode, title, year, brand, stock, price. Все данные сохраняются в базу данных.
4. Выводить список книг в виде таблицы (Bootstrap+Thymeleaf)
5. Поддерживать пагинацию и фильтрацию по title, brand, year
6. REST API: GET /api/books, POST /api/books, PUT /api/books/{id}, DELETE /api/books/{id}

Результат:

БД разворачивается в контейнере Docker, запустить docker-compouse.yml
- jdbc:postgresql://localhost:5433/books-db

### Администратор:
- Логин: admin
- Пароль: password
- Может создавать/редактировать/удалять книги
- http://localhost:8080/books/new
- http://localhost:8080/books/edit/1

Не аутентифицированный пользователь:
Может только просматривать книги
http://localhost:8080/books



### Запросы к REST API для тестирования доступ разрешен всем
- PUT: http://localhost:8080/api/books/{id}
- {
"vendorCode": "NH-2542",
"title": "Книга",
"brand": "А.С. Пушкин",
"year": 1255,
"stock": null,
"price": null
}

- DELETE: http://localhost:8080/api/books/{id}
- GET: http://localhost:8080/api/books
- GET: http://localhost:8080/api/books/{id}
- POST: http://localhost:8080/api/books
- {
"vendorCode": "NH-254",
"title": "Книга",
"brand": "А.С. Пушкин",
"year": 1255,
"stock": null,
"price": null
}