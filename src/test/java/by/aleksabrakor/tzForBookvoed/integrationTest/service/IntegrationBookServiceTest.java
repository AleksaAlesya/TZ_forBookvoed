package by.aleksabrakor.tzForBookvoed.integrationTest.service;

import by.aleksabrakor.tzForBookvoed.dto.BookDto;
import by.aleksabrakor.tzForBookvoed.entity.Book;
import by.aleksabrakor.tzForBookvoed.exception.NotCreatedException;
import by.aleksabrakor.tzForBookvoed.exception.NotFoundException;
import by.aleksabrakor.tzForBookvoed.repositiry.BookRepository;
import by.aleksabrakor.tzForBookvoed.service.BookService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class IntegrationBookServiceTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    public BookService bookService;

    @BeforeEach
    void setUp() {
        System.out.println("Очистка БД - настройка перед тестом");
        bookRepository.deleteAll();
    }

    @Test
    @DisplayName("Поиск книги, когда существует")
    void findBookById_ShouldReturnBookDto_WhenBookExists_() {
        //Подготовка тестовых данных
        Book book = new Book();
        book.setTitle("TestTitle");
        book.setBrand("TestBrand");
        book.setVendorCode("TestVendorCode");
        book.setYear(2020);

        Book savedBook = bookRepository.save(book);
        Long bookId = savedBook.getId();

        // Выполнение запроса и проверка результата
        BookDto resultBookDto = bookService.findBookById(bookId);

        assertNotNull(resultBookDto);
        assertEquals(bookId, resultBookDto.getId());
        assertEquals(book.getTitle(), resultBookDto.getTitle());
        assertEquals(book.getBrand(), resultBookDto.getBrand());
    }

    @Test
    @DisplayName("Поиск книги, когда не существует - выброс исключения")
    void findBookById_WhenBookNotExists_ShouldThrowNotFoundException() {
        Long bookId = 999L;

        // Выполнение запроса и проверка результата
        assertThrows(NotFoundException.class, () -> bookService.findBookById(bookId));
    }


    @Test
    @DisplayName("Поиск списка всех книг без заданных фильтров")
    void findAllBooks_ShouldReturnListOfBookDtos() {
        //Подготовка тестовых данных
        Book book1 = new Book();
        book1.setTitle("TestTitle");
        book1.setBrand("TestBrand");
        book1.setVendorCode("TestVendorCode");
        book1.setYear(2020);

        Book book2 = new Book();
        book2.setTitle("TestTitle2");
        book2.setBrand("TestBrand2");
        book2.setVendorCode("TestVendorCode2");
        book2.setYear(2010);

        List<Book> books = Arrays.asList(book1, book2);
        bookRepository.saveAll(books);

        // Выполнение запроса и проверка результата
        List<BookDto> resultBookDtos = bookService.findAllBooks();

        assertNotNull(resultBookDtos);
        assertTrue(resultBookDtos.size() >= 2);
        assertTrue(books.stream().anyMatch(s -> s.getTitle().equals(book1.getTitle())));
        assertTrue(books.stream().anyMatch(s -> s.getTitle().equals(book2.getTitle())));
    }

    @Test
    @DisplayName("Поиск списка всех книг, с фильтрами и пагинацией, если установили фильтр title, brand, year - все совпадают с существующей книгой")
    void findAllBooksWithFilters_ShouldReturnPagedResults_1() {
        //Подготовка тестовых данных
        Book book1 = new Book();
        book1.setTitle("TestTitle");
        book1.setBrand("TestBrand");
        book1.setVendorCode("TestVendorCode");
        book1.setYear(2020);

        Book book2 = new Book();
        book2.setTitle("TestTitle2");
        book2.setBrand("TestBrand2");
        book2.setVendorCode("TestVendorCode2");
        book2.setYear(2010);

        List<Book> books = Arrays.asList(book1, book2);
        bookRepository.saveAll(books);


        String title = "TestTitle2";
        String brand = "TestBrand2";
        Integer year = 2010;
        int page = 0;
        int size = 10;

        Page<BookDto> result = bookService.findAllBooks(title, brand, year, page, size);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }


    @Test
    @DisplayName("Поиск списка всех книг, с фильтрами и пагинацией, если установили фильтр title, brand, year - не совпадают с существующей книгой")
    void findAllBooksWithFilters_ShouldReturnPagedResults_2() {
        //Подготовка тестовых данных
        Book book1 = new Book();
        book1.setTitle("TestTitle");
        book1.setBrand("TestBrand");
        book1.setVendorCode("TestVendorCode");
        book1.setYear(2020);

        Book book2 = new Book();
        book2.setTitle("TestTitle2");
        book2.setBrand("TestBrand2");
        book2.setVendorCode("TestVendorCode2");
        book2.setYear(2010);

        List<Book> books = Arrays.asList(book1, book2);
        bookRepository.saveAll(books);


        String title = "TestTitle1";
        String brand = "TestBrand2";
        Integer year = 2010;
        int page = 0;
        int size = 10;

        Page<BookDto> result = bookService.findAllBooks(title, brand, year, page, size);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @DisplayName("Поиск списка всех книг, с фильтрами и пагинацией, если установили фильтр title  -  существует книга")
    void findAllBooksWithFilters_ShouldReturnPagedResults_3() {
        //Подготовка тестовых данных
        Book book1 = new Book();
        book1.setTitle("TestTitle");
        book1.setBrand("TestBrand");
        book1.setVendorCode("TestVendorCode");
        book1.setYear(2020);

        Book book2 = new Book();
        book2.setTitle("TestTitle2");
        book2.setBrand("TestBrand2");
        book2.setVendorCode("TestVendorCode2");
        book2.setYear(2010);

        List<Book> books = Arrays.asList(book1, book2);
        bookRepository.saveAll(books);


        String title = "TestTitle";
        String brand = null;
        Integer year = null;
        int page = 0;
        int size = 10;

        Page<BookDto> result = bookService.findAllBooks(title, brand, year, page, size);

        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 1);
    }

    @Test
    @DisplayName("Поиск списка всех книг, с фильтрами и пагинацией, если установили фильтр year  -  существует книга")
    void findAllBooksWithFilters_ShouldReturnPagedResults_4() {
        //Подготовка тестовых данных
        Book book1 = new Book();
        book1.setTitle("TestTitle");
        book1.setBrand("TestBrand");
        book1.setVendorCode("TestVendorCode");
        book1.setYear(2020);

        Book book2 = new Book();
        book2.setTitle("TestTitle2");
        book2.setBrand("TestBrand2");
        book2.setVendorCode("TestVendorCode2");
        book2.setYear(2010);

        List<Book> books = Arrays.asList(book1, book2);
        bookRepository.saveAll(books);


        String title = null;
        String brand = null;
        Integer year = 2010;
        int page = 0;
        int size = 10;

        Page<BookDto> result = bookService.findAllBooks(title, brand, year, page, size);

        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 1);
    }


    @Test
    @DisplayName("Сохранение книги, если VendorCode уникальный")
    void saveBook_WhenVendorCodeIsUnique_ShouldSaveAndReturnBookDto() {
        //Подготовка тестовых данных
        BookDto bookDto = new BookDto();
        bookDto.setTitle("TestTitle");
        bookDto.setBrand("TestBrand");
        bookDto.setVendorCode("TestVendorCode");
        bookDto.setYear(2020);

        // Выполнение запроса и проверка результата
        BookDto resultBookDto = bookService.saveBook(bookDto);

        assertNotNull(resultBookDto);
        assertNotNull(resultBookDto.getId());
        assertEquals(bookDto.getTitle(), resultBookDto.getTitle());
        assertEquals(bookDto.getBrand(), resultBookDto.getBrand());
        assertEquals(bookDto.getVendorCode(), resultBookDto.getVendorCode());
        assertEquals(bookDto.getYear(), resultBookDto.getYear());
    }

    @Test
    @DisplayName("Сохранение книги, если  VendorCode уже существует - NotCreatedException")
    void saveBook_WhenVendorCodeExists_ShouldThrowNotCreatedException() {
        //Подготовка тестовых данных
        BookDto bookDto = new BookDto();
        bookDto.setTitle("TestTitle");
        bookDto.setBrand("TestBrand");
        bookDto.setVendorCode("TestVendorCode");
        bookDto.setYear(2020);
        bookService.saveBook(bookDto);

        BookDto newBookDto = new BookDto();
        newBookDto.setTitle("TestTitle");
        newBookDto.setBrand("TestBrand");
        newBookDto.setVendorCode("TestVendorCode");
        newBookDto.setYear(2020);

        // Выполнение запроса и проверка результата
        assertThrows(NotCreatedException.class, () -> bookService.saveBook(newBookDto));
    }

    @Test
    @DisplayName("Обновление книги, если измененный VendorCode уникальный")
    void updateBook_WhenBookExistsAndVendorCodeIsUnique_ShouldUpdateAndReturnBookDto() {
        //Подготовка тестовых данных
        Book book = new Book();
        book.setVendorCode("VendorCode");
        book.setTitle("TestTitle");
        book.setBrand("TestBrand");
        book.setYear(2024);
        book.setStock(5);
        book.setPrice(BigDecimal.valueOf(150.00));

        Book savedBook = bookRepository.save(book);
        Long id = savedBook.getId();

        BookDto updatedDto = new BookDto();
        updatedDto.setId(id);
        updatedDto.setVendorCode("NEW-VENDOR");
        updatedDto.setTitle("Updated Title");
        updatedDto.setBrand("Updated Brand");
        updatedDto.setYear(2024);
        updatedDto.setStock(5);
        updatedDto.setPrice(BigDecimal.valueOf(150.00));

        // Выполнение запроса и проверка результата
        BookDto result = bookService.updateBook(id, updatedDto);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("NEW-VENDOR", result.getVendorCode());
    }

    @Test
    @DisplayName("Обновление книги, если измененный VendorCode неуникальный")
    void updateBook_WhenVendorCodeExistsForAnotherBook_ShouldThrowNotCreatedException() {
        //Подготовка тестовых данных
        Book book = new Book();
        book.setVendorCode("UniqueCode");
        book.setTitle("TestTitle");
        book.setBrand("TestBrand");
        book.setYear(2024);
        book.setStock(5);
        book.setPrice(BigDecimal.valueOf(150.00));

        Book updatingBook = new Book();
        updatingBook.setVendorCode("VendorCode1");
        updatingBook.setTitle("TestTitle");
        updatingBook.setBrand("TestBrand");
        updatingBook.setYear(2025);

        bookRepository.save(book);
        Book savedBook = bookRepository.save(updatingBook);
        Long id = savedBook.getId();

        BookDto updatedDto = new BookDto();
        updatedDto.setId(id);
        updatedDto.setVendorCode("UniqueCode"); // устанавливаем уже существующий
        updatedDto.setTitle("Updated Title");
        updatedDto.setBrand("Updated Brand");
        updatedDto.setYear(2024);
        updatedDto.setStock(5);
        updatedDto.setPrice(BigDecimal.valueOf(150.00));

        // Выполнение запроса и проверка результата
        assertThrows(NotCreatedException.class, () -> bookService.updateBook(id, updatedDto));
    }

    @Test
    @DisplayName("Обновление книги, если книга не найдена")
    void updateBook_WhenBookNotExists_ShouldThrowNotFoundException() {
        //Подготовка тестовых данных
        Long id = 999L;

        BookDto updatedDto = new BookDto();
        updatedDto.setId(id);
        updatedDto.setVendorCode("Updated Code");
        updatedDto.setTitle("Updated Title");
        updatedDto.setBrand("Updated Brand");
        updatedDto.setYear(2024);

        // Выполнение запроса и проверка результата
        assertThrows(NotFoundException.class, () -> bookService.updateBook(id, updatedDto));
    }

    @Test
    @DisplayName("Удаление книги, если книга существует")
    void deleteBook_WhenBookExists_ShouldDeleteBook() {
        //Подготовка тестовых данных
        Book book = new Book();
        book.setVendorCode("UniqueCode");
        book.setTitle("TestTitle");
        book.setBrand("TestBrand");
        book.setYear(2024);

        Book savedBook = bookRepository.save(book);
        Long id = savedBook.getId();

        // Выполнение запроса и проверка результата
        assertDoesNotThrow(() -> bookService.deleteBook(id));
    }

    @Test
    @DisplayName("Удаление книги, если книга не найдена")
    void deleteBook_WhenBookNotExists_ShouldThrowNotFoundException() {
        Long id = 999L;

        // Выполнение запроса и проверка результата
        assertThrows(NotFoundException.class, () -> bookService.deleteBook(id));
    }

    @Test
    @DisplayName("Проверка, если VendorCode уже существует (не уникальный)")
    void findByVendorCode_ShouldReturnOptionalOfBook() {
        //Подготовка тестовых данных
        String vendorCode = "UniqueCode";

        Book book = new Book();
        book.setVendorCode(vendorCode);
        book.setTitle("TestTitle");
        book.setBrand("TestBrand");
        book.setYear(2024);

        Book savedBook = bookRepository.save(book);
        Long id = savedBook.getId();

        // Выполнение запроса и проверка результата
        Optional<Book> result = bookService.findByVendorCode(vendorCode, 22L);
        assertTrue(result.isPresent());
        assertEquals(book, result.get());

        Optional<Book> result_2 = bookService.findByVendorCode(vendorCode, id);
        assertTrue(result_2.isEmpty());
    }

    @Test
    @DisplayName("Проверка, если VendorCode не найден - уникальный")
    void findByVendorCode_WhenNotFound_ShouldReturnEmptyOptional() {
        //Подготовка тестовых данных
        String vendorCode = "UniqueCode";

        Book book = new Book();
        book.setVendorCode("VendorCode");
        book.setTitle("TestTitle");
        book.setBrand("TestBrand");
        book.setYear(2024);

        bookRepository.save(book);

        // Выполнение запроса и проверка результата
        Optional<Book> result = bookService.findByVendorCode(vendorCode, 9L);

        assertTrue(result.isEmpty());
    }
}