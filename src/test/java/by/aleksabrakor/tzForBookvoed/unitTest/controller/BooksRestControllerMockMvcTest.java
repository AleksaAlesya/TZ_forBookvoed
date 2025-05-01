package by.aleksabrakor.tzForBookvoed.unitTest.controller;

import by.aleksabrakor.tzForBookvoed.aspect.ExceptionHandlerAdvice;
import by.aleksabrakor.tzForBookvoed.controller.BooksRestController;
import by.aleksabrakor.tzForBookvoed.dto.BookDto;
import by.aleksabrakor.tzForBookvoed.exception.NotFoundException;
import by.aleksabrakor.tzForBookvoed.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
class BooksRestControllerMockMvcTest {

    private MockMvc mockMvc;

    @Mock
    private BookService bookService; // Мок сервиса

    @InjectMocks
    private BooksRestController booksRestController; // Тестируемый контроллер с внедренным моком

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(booksRestController)
                .setControllerAdvice(new ExceptionHandlerAdvice())
                .build();
    }

    @Test
    @DisplayName("Поиск списка всех книг")
    void getAllBooks_shouldReturnListOfBooks() throws Exception {
        // Подготовка тестовых данных
        BookDto bookDto1 = new BookDto();
        BookDto bookDto2 = new BookDto();
        List<BookDto> bookDtos = Arrays.asList(bookDto1, bookDto2);

        // Настройка mock-объекта
        when(bookService.findAllBooks()).thenReturn(bookDtos);

        // Выполнение запроса и проверка результата
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
        ;
        verify(bookService, times(1)).findAllBooks();
    }

    @Test
    @DisplayName("Поиск списка всех книг, если список пуст")
    void getAllBooks_ShouldReturnEmptyList() throws Exception {
        // Настройка mock-объекта
        when(bookService.findAllBooks()).thenReturn(Collections.emptyList());

        // Выполнение запроса и проверка результата
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        verify(bookService, times(1)).findAllBooks();
    }

    @Test
    @DisplayName("Поиск книги по существующему id")
    void findBookById_ShouldReturnBook_WhenBookExists() throws Exception {
        // Подготовка тестовых данных
        Long id = 1L;
        BookDto bookDto = new BookDto();
        bookDto.setId(id);
        bookDto.setTitle("Book");
        bookDto.setBrand("Author 1");
        bookDto.setYear(1984);

        // Настройка mock-объекта
        when(bookService.findBookById(id)).thenReturn(bookDto);

        // Выполнение запроса и проверка результата
        mockMvc.perform(get("/api/books/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Book"))
                .andExpect(jsonPath("$.brand").value("Author 1"))
                .andExpect(jsonPath("$.year").value(1984))
        ;
        verify(bookService, times(1)).findBookById(id);
    }

    @Test
    @DisplayName("Вернет NotFound 404 при поиске книги, если id не существует")
    void findBookById_ShouldThrowNotFoundException_WhenBookNotExists_() throws Exception {
        // Подготовка тестовых данных
        Long id = 22L;

        // Настройка mock-объекта
        when(bookService.findBookById(id)).thenThrow(new NotFoundException("Книга с id: " + id + " не найдена."));

        // Выполнение запроса и проверка результата
        mockMvc.perform(get("/api/books/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Object was not found: Книга с id: " + id + " не найдена."));
        verify(bookService, times(1)).findBookById(id);
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

        BookDto savedBook = new BookDto();
        savedBook.setId(1L);
        savedBook.setVendorCode("b-125");
        savedBook.setTitle("Book");
        savedBook.setBrand("Author 1");
        savedBook.setYear(1984);

        // Настройка mock-объекта
        when(bookService.saveBook(any(BookDto.class))).thenReturn(savedBook);

        // Выполнение запроса и проверка результата
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Book"))
                .andExpect(jsonPath("$.brand").value("Author 1"))
                .andExpect(jsonPath("$.year").value(1984))
        ;
        verify(bookService, times(1)).saveBook(any(BookDto.class));
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
        Long bookId = 1L;

        String bookJson = """
                {
                     "title": "update Book",
                     "brand": "update Author 1"
                }
                """;

        BookDto updatedBook = new BookDto();
        updatedBook.setId(bookId);
        updatedBook.setTitle("update Book");
        updatedBook.setBrand("update Author 1");
        updatedBook.setVendorCode("b-125");
        updatedBook.setYear(1984);

        // Настройка mock-объекта
        when(bookService.updateBook(eq(bookId), any(BookDto.class))).thenReturn(updatedBook);

        // Выполнение запроса и проверка результата
        mockMvc.perform(put("/api/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId))
                .andExpect(jsonPath("$.title").value("update Book"))
                .andExpect(jsonPath("$.brand").value("update Author 1"))
                .andExpect(jsonPath("$.year").value(1984))
        ;
        verify(bookService, times(1)).updateBook(eq(bookId), any(BookDto.class));
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

        // Настройка mock-объекта
        when(bookService.updateBook(eq(bookId), any(BookDto.class))).thenThrow(new NotFoundException("Книга с id: " + bookId + " не найдена."));

        // Выполнение запроса и проверка результата
        mockMvc.perform(put("/api/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Object was not found: Книга с id: " + bookId + " не найдена."))
        ;
        verify(bookService, times(1)).updateBook(eq(bookId), any(BookDto.class));
    }

    @Test
    @DisplayName("Удаление задачи, по существующему id")
    void deleteBook__ShouldDeleteBook_WhenBookExist() throws Exception {
        // Подготовка тестовых данных
        // Создаем пользователя в БД
        Long bookId = 1L;

        // Настройка mock-объекта
        doNothing().when(bookService).deleteBook(bookId);

        // Выполнение запроса и проверка результата
        mockMvc.perform(delete("/api/books/{id}", bookId))
                .andExpect(status().isOk())
        ;
        verify(bookService, times(1)).deleteBook(bookId);
    }


    @Test
    @DisplayName("Вернет NotFound 404 при попытке удаление книги, по  не существующему id")
    void deleteBook_ShouldReturns404_WhenBookNotExists() throws Exception {
        // Подготовка тестовых данных
        Long bookId = 999L;

        // Настраиваем мок, чтобы при вызове deleteUser с несуществующим ID выбрасывалось исключение
        doThrow(new NotFoundException("Книга с id: " + bookId + " не найдена."))
                .when(bookService).deleteBook(bookId);

        // Выполнение запроса и проверка результата
        mockMvc.perform(delete("/api/books/{id}", bookId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Object was not found: Книга с id: " + bookId + " не найдена."))
        ;
        verify(bookService, times(1)).deleteBook(bookId);
    }
}