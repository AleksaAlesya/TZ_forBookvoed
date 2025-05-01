package by.aleksabrakor.tzForBookvoed.integrationTest.controller;

import by.aleksabrakor.tzForBookvoed.dto.BookDto;
import by.aleksabrakor.tzForBookvoed.repositiry.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class IntegrationBooksRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        System.out.println("Очистка БД - настройка перед тестом");
        bookRepository.deleteAll();
    }


    @Test
    @DisplayName("Поиск списка всех книг")
    void getAllBooks_shouldReturnListOfBooks() throws Exception {
        // Подготовка тестовых данных
        String bookJson1 = """
                {
                    "vendorCode": "b-125",
                     "title": "Book1",
                     "brand": "Author 1",
                     "year": 1984
                }
                """;
        String bookJson2 = """
                {
                    "vendorCode": "b-125-2",
                     "title": "Book2",
                     "brand": "Author 2",
                     "year": 1984
                }
                """;

        //сохраняем в бд тестовые данные
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson1));
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson2));

        // Выполнение запроса и проверка результата
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Book1"))
                .andExpect(jsonPath("$[0].brand").value("Author 1"))
                .andExpect(jsonPath("$[0].year").value(1984))
                .andExpect(jsonPath("$[1].title").value("Book2"))
                .andExpect(jsonPath("$[1].brand").value("Author 2"))
                .andExpect(jsonPath("$[1].year").value(1984))
        ;
    }

    @Test
    @DisplayName("Поиск списка всех книг, если список пуст")
    void getAllBooks_ShouldReturnEmptyList() throws Exception {
        // Выполнение запроса и проверка результата
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty())
        ;
    }

    @Test
    @DisplayName("Поиск книги по существующему id")
    void findBookById_ShouldReturnBook_WhenBookExists() throws Exception {
        // Подготовка тестовых данных
        // Подготовка тестовых данных
        String bookJson1 = """
                {
                    "vendorCode": "b-125",
                     "title": "Book1",
                     "brand": "Author 1",
                     "year": 1984
                }
                """;

        //сохраняем в бд тестовые данные
        String createBook = mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson1)).andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readValue(createBook, BookDto.class).getId();

        // Выполнение запроса и проверка результата
        mockMvc.perform(get("/api/books/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Book1"))
                .andExpect(jsonPath("$.brand").value("Author 1"))
                .andExpect(jsonPath("$.year").value(1984))
        ;
    }

    @Test
    @DisplayName("Вернет NotFound 404 при поиске книги, если id не существует")
    void findBookById_ShouldThrowNotFoundException_WhenBookNotExists_() throws Exception {
        // Подготовка тестовых данных
        Long id = 999L;

        // Выполнение запроса и проверка результата
        mockMvc.perform(get("/api/books/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Object was not found: Книга с id: " + id + " не найдена."))
        ;
    }

    @Test
    @DisplayName("Сохранение новой книги")
    void createBook_shouldReturnCreatedBook() throws Exception {
        // Подготовка тестовых данных
        String bookJson = """
                {
                    "vendorCode": "b-125",
                     "title": "Book",
                     "brand": "Author 1",
                     "year": 1984
                }
                """;

        // Выполнение запроса и проверка результата
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Book"))
                .andExpect(jsonPath("$.brand").value("Author 1"))
                .andExpect(jsonPath("$.year").value(1984))
        ;
    }

    @Test
    @DisplayName("Вернет BadRequest 400 при попытке сохранения юзера, с невалидными полями")
    void createBook_ShouldReturnBadRequest400_WhenInvalidInput() throws Exception {
        // Подготовка тестовых данных
        String bookJson = """
                {
                    "vendorCode": "b-125",
                     "title": "Book",
                     "brand": null,
                     "year": null
                }
                """;

        // Выполнение запроса и проверка результата
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("Обновление книги, по существующему id")
    void updateBook_ShouldReturnBook_WhenBookExists() throws Exception {
        // Подготовка тестовых данных
        String bookJson = """
                {
                    "vendorCode": "b-125",
                     "title": "Book",
                     "brand": "Author 1",
                     "year": 1984
                }
                """;

        // Выполнение запроса и проверка результата
        String createBookResponse = mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson)).andReturn().getResponse().getContentAsString();

        Long bookId = objectMapper.readValue(createBookResponse, BookDto.class).getId();

        String updateBookJson = """
                {
                     "title": "update Book",
                     "brand": "update Author 1"
                }
                """;


        // Выполнение запроса и проверка результата
        mockMvc.perform(put("/api/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBookJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId))
                .andExpect(jsonPath("$.title").value("update Book"))
                .andExpect(jsonPath("$.brand").value("update Author 1"))
                .andExpect(jsonPath("$.year").value(1984))
        ;
    }

    @Test
    @DisplayName("Вернет NotFound 404 при попытке обновления книги, если id не существует")
    void updateBook_ShouldReturns404_WhenBookNotExists() throws Exception {
        // Подготовка тестовых данных
        Long bookId = 999L;

        String bookJson = """
                {
                     "title": "update Book",
                     "brand": "update Author 1"
                }
                """;

        // Выполнение запроса и проверка результата
        mockMvc.perform(put("/api/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Object was not found: Книга с id: " + bookId + " не найдена."))
        ;
    }

    @Test
    @DisplayName("Удаление задачи, по существующему id")
    void deleteBook__ShouldDeleteBook_WhenBookExist() throws Exception {
        // Подготовка тестовых данных
        // Создаем пользователя в БД
        String bookJson = """
                {
                    "vendorCode": "b-125",
                     "title": "Book",
                     "brand": "Author 1",
                     "year": 1984
                }
                """;

        String createBookResponse = mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson)).andReturn().getResponse().getContentAsString();

        Long bookId = objectMapper.readValue(createBookResponse, BookDto.class).getId();

        // Выполнение запроса и проверка результата
        mockMvc.perform(delete("/api/books/{id}", bookId))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("Вернет NotFound 404 при попытке удаление книги, по  не существующему id")
    void deleteBook_ShouldReturns404_WhenBookNotExists() throws Exception {
        // Подготовка тестовых данных
        Long bookId = 999L;

        // Выполнение запроса и проверка результата
        mockMvc.perform(delete("/api/books/{id}", bookId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Object was not found: Книга с id: " + bookId + " не найдена."))
        ;
    }
}