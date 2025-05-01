package by.aleksabrakor.tzForBookvoed.unitTest.controller;

import by.aleksabrakor.tzForBookvoed.aspect.ExceptionHandlerAdvice;
import by.aleksabrakor.tzForBookvoed.controller.BooksWebController;
import by.aleksabrakor.tzForBookvoed.dto.BookDto;
import by.aleksabrakor.tzForBookvoed.service.BookService;
import by.aleksabrakor.tzForBookvoed.util.BookValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.security.Principal;
import java.util.Collections;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BooksWebControllerTest {

    @Mock
    private BookService bookService;

    @Mock
    private BookValidator bookValidator;

    @InjectMocks
    private BooksWebController booksWebController;

    private MockMvc mockMvc;

    @Mock
    private Principal principal;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(booksWebController)
                .setControllerAdvice(new ExceptionHandlerAdvice())
                .build();
    }

    @Test
    void findAllBooks_shouldReturnViewWithBooks() throws Exception {
        // Arrange
        Page<BookDto> page = new PageImpl<>(Collections.singletonList(new BookDto()));

        when(bookService.findAllBooks(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/books")
                        .param("page", "0")
                        .param("size", "10")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attribute("title", nullValue()))
                .andExpect(model().attribute("brand", nullValue()))
                .andExpect(model().attribute("year", nullValue()));
        verify(bookService).findAllBooks(any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void findAllBooks_withFilters_shouldReturnFilteredBooks() throws Exception {
        // Arrange
        Page<BookDto> page = new PageImpl<>(Collections.singletonList(new BookDto()));
        when(bookService.findAllBooks(eq("test"), eq("testBrand"), eq(2023), anyInt(), anyInt()))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/books")
                        .param("title", "test")
                        .param("brand", "testBrand")
                        .param("year", "2023")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("title", "test"))
                .andExpect(model().attribute("brand", "testBrand"))
                .andExpect(model().attribute("year", 2023));
        verify(bookService).findAllBooks(eq("test"), eq("testBrand"), eq(2023), anyInt(), anyInt());
    }

    @Test
    void showCreateForm_shouldReturnFormView() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeExists("book"));
    }

    @Test
    void saveBook_withValidData_shouldRedirectToList() throws Exception {
        // Arrange
        BookDto bookDto = new BookDto();
        bookDto.setId(1L);
        bookDto.setVendorCode("b-125");
        bookDto.setTitle("test");
        bookDto.setBrand("testBrand");
        bookDto.setYear(2023);

        when(bookService.saveBook(any(BookDto.class))).thenReturn(bookDto);

        // Act & Assert
        mockMvc.perform(post("/books/save")
                        .flashAttr("book", bookDto)
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
        verify(bookService, times(1)).saveBook(any(BookDto.class));
    }

    @Test
    void saveBook_withInvalidData_shouldReturnFormView() throws Exception {
        // Arrange
        BookDto bookDto = new BookDto();

        // Act & Assert
        mockMvc.perform(post("/books/save")
                        .flashAttr("book", bookDto)
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeHasFieldErrors("book", "title", "brand", "vendorCode", "year"));
    }

    @Test
    void showEditForm_shouldReturnFormViewWithBook() throws Exception {
        // Arrange
        BookDto bookDto = new BookDto();
        when(bookService.findBookById(any(Long.class))).thenReturn(bookDto);

        // Act & Assert
        mockMvc.perform(get("/books/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeExists("book"));
    }

    @Test
    void updateBook_withValidData_shouldRedirectToList() throws Exception {
        // Arrange
        BookDto bookDto = new BookDto();

        when(bookService.updateBook(any(Long.class), any(BookDto.class))).thenReturn(bookDto);

        // Act & Assert
        mockMvc.perform(post("/books/update/1")
                        .flashAttr("book", bookDto)
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
        verify(bookService, times(1)).updateBook(any(Long.class), any(BookDto.class));
    }

    @Test
    void updateBook_withInvalidData_shouldReturnFormView() throws Exception {
        // Arrange
        BookDto bookDto = new BookDto();

//         Настраиваем мок валидатора, чтобы он добавлял ошибки
        doAnswer(invocation -> {
            BindingResult bindingResult = invocation.getArgument(1);
            bindingResult.addError(new FieldError("book", "title", "Title is required"));
            return null;
        }).when(bookValidator).validate(any(BookDto.class), any(BindingResult.class));

        // Act & Assert
        mockMvc.perform(post("/books/update/1")
                        .flashAttr("book", bookDto)
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeHasErrors("book"))
                .andExpect(model().attributeHasFieldErrors("book", "title"));
    }

    @Test
    void deleteBook_shouldRedirectToList() throws Exception {
        // Arrange
        doNothing().when(bookService).deleteBook(any(Long.class));

        // Act & Assert
        mockMvc.perform(get("/books/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
        verify(bookService, times(1)).deleteBook(any(Long.class));
    }

    @Test
    void processBook_withErrors_shouldReturnFalse() {
        // Arrange
        BookDto bookDto = new BookDto();
        when(bindingResult.hasErrors()).thenReturn(true);

        // Act
        Boolean result = booksWebController.processBook(bookDto, bindingResult);

        // Assert
        assertFalse(result);
        verify(bookValidator).validate(bookDto, bindingResult);
    }

    @Test
    void processBook_withoutErrors_shouldReturnTrue() {
        // Arrange
        BookDto bookDto = new BookDto();
        when(bindingResult.hasErrors()).thenReturn(false);

        // Act
        boolean result = booksWebController.processBook(bookDto, bindingResult);

        // Assert
        assertTrue(result);
        verify(bookValidator).validate(bookDto, bindingResult);
    }
}