package by.aleksabrakor.tzForBookvoed.unitTest.service;

import by.aleksabrakor.tzForBookvoed.dto.BookDto;
import by.aleksabrakor.tzForBookvoed.entity.Book;
import by.aleksabrakor.tzForBookvoed.exception.NotCreatedException;
import by.aleksabrakor.tzForBookvoed.exception.NotFoundException;
import by.aleksabrakor.tzForBookvoed.mapper.BookMapper;

import by.aleksabrakor.tzForBookvoed.repositiry.BookRepository;
import by.aleksabrakor.tzForBookvoed.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    @BeforeEach
    void setUp() {

    }

    @Test
    @DisplayName("Поиск книги, когда существует")
    void findBookById_WhenBookExists_ShouldReturnBookDto() {
        Long bookId = 1L;

        Book mockBook = new Book();
        mockBook.setId(bookId);
        mockBook.setTitle("Book Title");
        mockBook.setBrand("TestBrand");

        BookDto mockBookDto = new BookDto();
        mockBookDto.setId(bookId);
        mockBookDto.setTitle("Book Title");
        mockBookDto.setBrand("TestBrand");

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(mockBook));
        when(bookMapper.entityToDto(mockBook)).thenReturn(mockBookDto);

        BookDto resultBookDto = bookService.findBookById(bookId);

        assertNotNull(resultBookDto);
        assertEquals(bookId, resultBookDto.getId());
        assertEquals(mockBook.getTitle(), resultBookDto.getTitle());
        assertEquals(mockBook.getBrand(), resultBookDto.getBrand());
        verify(bookRepository).findById(bookId);
        verify(bookMapper).entityToDto(mockBook);
    }

    @Test
    @DisplayName("Поиск книги, когда не существует - выброс исключения")
    void findBookById_WhenBookNotExists_ShouldThrowNotFoundException() {
        Long bookId = 1L;

        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.findBookById(bookId));
        verify(bookRepository).findById(bookId);
    }


    @Test
    @DisplayName("Поиск списка всех книг без заданных фильтров")
    void findAllBooks_ShouldReturnListOfBookDtos() {
        Book mockBook1 = new Book();
        Book mockBook2 = new Book();
        List<Book> mockBooks = Arrays.asList(mockBook1, mockBook2);

        BookDto mockBookDto1 = new BookDto();
        BookDto mockBookDto2 = new BookDto();
        List<BookDto> mockBookDtos = Arrays.asList(mockBookDto1, mockBookDto2);

        when(bookRepository.findAll()).thenReturn(mockBooks);
        when(bookMapper.toDtoList(mockBooks)).thenReturn(mockBookDtos);

        List<BookDto> resultBookDtos = bookService.findAllBooks();

        assertNotNull(resultBookDtos);
        assertEquals(mockBooks.size(), resultBookDtos.size());
        verify(bookRepository).findAll();
        verify(bookMapper).toDtoList(mockBooks);
    }

    @Test
    @DisplayName("Поиск списка всех книг, с фильтрами и пагинацией")
    void findAllBooksWithFilters_ShouldReturnPagedResults() {
        String title = "Test";
        String brand = "Brand";
        Integer year = 2023;
        int page = 0;
        int size = 10;

        Book mockBook1 = new Book();
        Book mockBook2 = new Book();
        List<Book> mockBooks = Arrays.asList(mockBook1, mockBook2);

        BookDto mockBookDto1 = new BookDto();
        BookDto mockBookDto2 = new BookDto();
        List<BookDto> mockBookDtos = Arrays.asList(mockBookDto1, mockBookDto2);

        Pageable pageable = PageRequest.of(page, size, Sort.by("title"));

        Page<Book> bookPage = new PageImpl<>(mockBooks, pageable, 1);
        Page<BookDto> bookDtoPage = new PageImpl<>(mockBookDtos, pageable, 1);

        when(bookRepository.findByFilters(title, brand, year, pageable)).thenReturn(bookPage);
        when(bookMapper.toDtoPage(bookPage)).thenReturn(bookDtoPage);

        Page<BookDto> result = bookService.findAllBooks(title, brand, year, page, size);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(bookRepository).findByFilters(title, brand, year, pageable);
        verify(bookMapper).toDtoPage(bookPage);
    }

    @Test
    @DisplayName("Сохранение книги, если VendorCode уникальный")
    void saveBook_WhenVendorCodeIsUnique_ShouldSaveAndReturnBookDto() {
        BookDto mockBookDto = new BookDto();
        mockBookDto.setVendorCode("VendorCode");
        mockBookDto.setTitle("Book Title");
        mockBookDto.setBrand("TestBrand");

        Book mockBook = new Book();
        mockBook.setId(1L);
        mockBook.setVendorCode("VendorCode");
        mockBook.setTitle("Book Title");
        mockBook.setBrand("TestBrand");

        BookDto mockBookDtoAfterSave = new BookDto();
        mockBookDtoAfterSave.setId(1L);
        mockBookDtoAfterSave.setVendorCode("VendorCode");
        mockBookDtoAfterSave.setTitle("Book Title");
        mockBookDtoAfterSave.setBrand("TestBrand");


        when(bookRepository.findByVendorCode(mockBookDto.getVendorCode())).thenReturn(Optional.empty());
        when(bookMapper.dtoToEntity(mockBookDto)).thenReturn(mockBook);
        when(bookRepository.save(mockBook)).thenReturn(mockBook);
        when(bookMapper.entityToDto(mockBook)).thenReturn(mockBookDtoAfterSave);


        BookDto result = bookService.saveBook(mockBookDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(bookRepository).findByVendorCode(mockBook.getVendorCode());
        verify(bookMapper).dtoToEntity(mockBookDto);
        verify(bookRepository).save(mockBook);
        verify(bookMapper).entityToDto(mockBook);
    }

    @Test
    @DisplayName("Сохранение книги, если  VendorCode уже существует - NotCreatedException")
    void saveBook_WhenVendorCodeExists_ShouldThrowNotCreatedException() {
        BookDto mockBookDto = new BookDto();
        mockBookDto.setVendorCode("VendorCode");
        mockBookDto.setTitle("Book Title");
        mockBookDto.setBrand("TestBrand");

        when(bookRepository.findByVendorCode(mockBookDto.getVendorCode())).thenReturn(Optional.of(new Book()));

        assertThrows(NotCreatedException.class, () -> bookService.saveBook(mockBookDto));
        verify(bookRepository).findByVendorCode(mockBookDto.getVendorCode());
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("Обновление книги, если измененный VendorCode уникальный")
    void updateBook_WhenBookExistsAndVendorCodeIsUnique_ShouldUpdateAndReturnBookDto() {
        Long id = 1L;
        Book mockBook = new Book();
        mockBook.setId(id);
        mockBook.setVendorCode("VendorCode");
        mockBook.setTitle("TestTitle");
        mockBook.setBrand("TestBrand");
        mockBook.setYear(2024);
        mockBook.setStock(5);
        mockBook.setPrice(BigDecimal.valueOf(150.00));


        BookDto updatedDto = new BookDto();
        updatedDto.setId(id);
        updatedDto.setVendorCode("NEW-VENDOR");
        updatedDto.setTitle("Updated Title");
        updatedDto.setBrand("Updated Brand");
        updatedDto.setYear(2024);
        updatedDto.setStock(5);
        updatedDto.setPrice(BigDecimal.valueOf(150.00));

        when(bookRepository.findById(id)).thenReturn(Optional.of(mockBook));
        when(bookRepository.findByVendorCodeAndIdNot("NEW-VENDOR", id)).thenReturn(Optional.empty());
        when(bookMapper.entityToDto(mockBook)).thenReturn(updatedDto);

        BookDto result = bookService.updateBook(id, updatedDto);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("NEW-VENDOR", result.getVendorCode());
        verify(bookRepository).findById(id);
        verify(bookRepository).findByVendorCodeAndIdNot("NEW-VENDOR", id);
    }

    @Test
    @DisplayName("Обновление книги, если измененный VendorCode неуникальный")
    void updateBook_WhenVendorCodeExistsForAnotherBook_ShouldThrowNotCreatedException() {

        Long id = 1L;

        Book mockBook = new Book();
        mockBook.setId(id);

        BookDto updatedDto = new BookDto();
        updatedDto.setVendorCode("EXISTING-VENDOR");

        when(bookRepository.findById(id)).thenReturn(Optional.of(mockBook));
        when(bookRepository.findByVendorCodeAndIdNot("EXISTING-VENDOR", id))
                .thenReturn(Optional.of(new Book()));

        assertThrows(NotCreatedException.class, () -> bookService.updateBook(id, updatedDto));
        verify(bookRepository).findById(id);
        verify(bookRepository).findByVendorCodeAndIdNot("EXISTING-VENDOR", id);
    }

    @Test
    @DisplayName("Обновление книги, если книга не найдена")
    void updateBook_WhenBookNotExists_ShouldThrowNotFoundException() {
        Long id = 1L;
        BookDto updatedDto = new BookDto();
        updatedDto.setVendorCode("EXISTING-VENDOR");

        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.updateBook(id, updatedDto));
        verify(bookRepository).findById(id);
        verify(bookRepository, never()).findByVendorCodeAndIdNot(any(), any());
    }

    @Test
    @DisplayName("Удаление книги, если книга существует")
    void deleteBook_WhenBookExists_ShouldDeleteBook() {
        Long id = 1L;
        Book mockBook = new Book();
        mockBook.setId(id);

        when(bookRepository.findById(id)).thenReturn(Optional.of(mockBook));
        doNothing().when(bookRepository).delete(mockBook);

        assertDoesNotThrow(() -> bookService.deleteBook(id));
        verify(bookRepository).findById(id);
        verify(bookRepository).delete(mockBook);
    }

    @Test
    @DisplayName("Удаление книги, если книга не найдена")
    void deleteBook_WhenBookNotExists_ShouldThrowNotFoundException() {
        Long id = 1L;
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.deleteBook(id));
        verify(bookRepository).findById(id);
        verify(bookRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Проверка, если VendorCode уже существует (не уникальный)")
    void findByVendorCode_ShouldReturnOptionalOfBook() {
        Long id = 1L;
        String vendorCode = "VendorCode";

        Book mockBook = new Book();
        mockBook.setId(id);
        mockBook.setVendorCode("VendorCode");

        when(bookRepository.findByVendorCodeAndIdNot(vendorCode, id)).thenReturn(Optional.of(mockBook));

        Optional<Book> result = bookService.findByVendorCode(vendorCode, id);

        assertTrue(result.isPresent());
        assertEquals(mockBook, result.get());
        verify(bookRepository).findByVendorCodeAndIdNot(vendorCode, id);
    }

    @Test
    @DisplayName("Проверка, если VendorCode не найден - уникальный")
    void findByVendorCode_WhenNotFound_ShouldReturnEmptyOptional() {
        Long id = 1L;
        String vendorCode = "VendorCode";

        when(bookRepository.findByVendorCodeAndIdNot(vendorCode, id)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.findByVendorCode(vendorCode, id);

        assertFalse(result.isPresent());
        verify(bookRepository).findByVendorCodeAndIdNot(vendorCode, id);
    }
}