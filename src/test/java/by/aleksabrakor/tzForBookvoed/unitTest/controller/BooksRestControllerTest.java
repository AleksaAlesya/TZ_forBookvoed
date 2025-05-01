package by.aleksabrakor.tzForBookvoed.unitTest.controller;

import by.aleksabrakor.tzForBookvoed.controller.BooksRestController;
import by.aleksabrakor.tzForBookvoed.dto.BookDto;
import by.aleksabrakor.tzForBookvoed.exception.NotCreatedException;
import by.aleksabrakor.tzForBookvoed.exception.NotFoundException;
import by.aleksabrakor.tzForBookvoed.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class BooksRestControllerTest {

    @Mock
    private BookService bookService; // Мок сервиса

    @InjectMocks
    private BooksRestController booksRestController; // Тестируемый контроллер с внедренным моком


    @Test
    @DisplayName("Поиск списка всех книг")
    void getAllBooks_shouldReturnListOfBooks() {
        //Arrange
        BookDto bookDto1 = new BookDto();
        BookDto bookDto2 = new BookDto();
        List<BookDto> bookDtos = Arrays.asList(bookDto1, bookDto2);

        when(bookService.findAllBooks()).thenReturn(bookDtos);

        //Act
        List<BookDto> resultList = booksRestController.getAllBooks();

        //Assert
        assertEquals(bookDtos.size(), resultList.size());
        assertEquals(bookDtos, resultList);
        verify(bookService, times(1)).findAllBooks();
    }

    @Test
    @DisplayName("Поиск книги по существующему id")
    void findBookById_WhenBookExists_ShouldReturnBookDto() {
        //Arrange
        Long id = 1L;
        BookDto bookDto = new BookDto();
        bookDto.setId(id);
        bookDto.setTitle("Book");
        bookDto.setBrand("Author 1");
        bookDto.setYear(1984);

        when(bookService.findBookById(id)).thenReturn(bookDto);

        //Act
        BookDto resultBookDto = bookService.findBookById(id);

        //Assert
        assertEquals(bookDto, resultBookDto);
        verify(bookService, times(1)).findBookById(id);
    }

    @Test
    @DisplayName("Поиск книги, если  id не существует ")
    void findBookById_WhenBookNotExists_ShouldThrowNotFoundException() {
        //Arrange
        Long id = 22L;
        when(bookService.findBookById(id)).thenThrow(new NotFoundException("Object was not found:"));

        //Act
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookService.findBookById(id));

        //Assert
        assertEquals("Object was not found:", exception.getMessage());
    }

    @Test
    void createBook_shouldReturnCreatedBook() {
        //Arrange
        BookDto bookDto = new BookDto();
        bookDto.setId(null);
        bookDto.setVendorCode("b-125");
        bookDto.setTitle("Book");
        bookDto.setBrand("Author 1");
        bookDto.setYear(1984);

        BookDto savedBook = new BookDto();
        savedBook.setId(1L);
        savedBook.setVendorCode("b-125");
        savedBook.setTitle("Book");
        savedBook.setBrand("Author 1");
        savedBook.setYear(1984);

        when(bookService.saveBook(bookDto)).thenReturn(savedBook);

        //Act
        BookDto resultBookDto = booksRestController.createBook(bookDto);

        //Assert
        assertEquals(savedBook, resultBookDto);
        verify(bookService, times(1)).saveBook(bookDto);
    }

    @Test
    void createBook_ShouldThrowNotCreatedException_WhenCreationFails() {
        // Arrange
        // невалидные данные
        BookDto invalidBook = new BookDto();
        invalidBook.setId(null);
        invalidBook.setVendorCode(null);
        invalidBook.setTitle(null);
        invalidBook.setBrand("");
        invalidBook.setYear(null);

        when(bookService.saveBook(invalidBook)).thenThrow(new NotCreatedException("Validation failed:"));

        // Act & Assert
        NotCreatedException exception = assertThrows(NotCreatedException.class, () -> {
            booksRestController.createBook(invalidBook);
        });

        assertEquals("Validation failed:", exception.getMessage());
    }


    @Test
    void updateBook_shouldReturnUpdatedBook() {
        // Arrange
        Long bookId = 1L;
        BookDto bookDto = new BookDto();
        bookDto.setId(bookId);
        bookDto.setTitle("Book");
        bookDto.setBrand("Author 1");
        bookDto.setYear(1984);

        BookDto updatedBook = new BookDto();
        updatedBook.setId(bookId);
        updatedBook.setTitle("Book");
        updatedBook.setBrand("Author 1");
        updatedBook.setYear(1984);

        when(bookService.updateBook(bookId, bookDto)).thenReturn(updatedBook);

        // Act
        BookDto result = booksRestController.updateBook(bookId, bookDto);

        // Assert
        assertEquals(updatedBook, result);
        verify(bookService, times(1)).updateBook(bookId, bookDto);
    }

    @Test
    void deleteBook_shouldReturnOkStatus() {
        // Arrange
        Long bookId = 1L;
        doNothing().when(bookService).deleteBook(bookId);

        // Act
        ResponseEntity<?> response = booksRestController.deleteBook(bookId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(bookService, times(1)).deleteBook(bookId);
    }
}