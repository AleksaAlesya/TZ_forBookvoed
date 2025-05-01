package by.aleksabrakor.tzForBookvoed.integrationTest.controller;

import by.aleksabrakor.tzForBookvoed.dto.BookDto;
import by.aleksabrakor.tzForBookvoed.entity.Book;
import by.aleksabrakor.tzForBookvoed.repositiry.BookRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class IntegrationBooksWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        System.out.println("Очистка БД - настройка перед тестом");
        bookRepository.deleteAll();
    }

    // Вспомогательные методы для создания тестовых данных
    private Book saveInDbValidTestBook(String vendorCode, String title, String brand, int year) {
        Book book = new Book();
        book.setTitle(title);
        book.setBrand(brand);
        book.setVendorCode(vendorCode);
        book.setYear(year);
        return bookRepository.save(book);
    }
    private BookDto createValidBookDto() {
        return BookDto.builder()
                .vendorCode("b-001")
                .title("Test Book")
                .brand("Test Brand")
                .year(2023)
                .build();
    }

    @Test
    @DisplayName("Поиск списка всех книг, если фильтры не заданы")
    void findAllBooks_shouldReturnPaginatedResults() throws Exception {
        saveInDbValidTestBook("b-001", "Book 1", "Brand A", 2020);
        saveInDbValidTestBook("b-002", "Book 2", "Brand B", 2025);

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attribute("title", nullValue()))
                .andExpect(model().attribute("brand", nullValue()))
                .andExpect(model().attribute("year", nullValue()));
    }

    @Test
    @DisplayName("Поиск списка всех книг, с фильтрами ")
    void findAllBooks_withFilters_shouldFilterResults() throws Exception {
        saveInDbValidTestBook("b-001", "Spring", "Manning", 2020);
        saveInDbValidTestBook("b-002", "Book 2", "Brand B", 2025);

        mockMvc.perform(get("/books")
                        .param("title", "Spring")
                        .param("brand", "Manning")
                        .param("year", "2020"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("title", "Spring"))
                .andExpect(model().attribute("brand", "Manning"))
                .andExpect(model().attribute("year", 2020));
    }

    @Test
    @WithMockUser
    @DisplayName("Поиск списка всех книг, для аутентифицированного пользователя")
    void findAllBooks_authenticated_shouldAddUsername() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("username"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void showCreateForm_authenticatedUser_shouldReturnForm() throws Exception {
        mockMvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeExists("book"));
    }

    @Test
    void showCreateForm_unauthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/books/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void saveBook_validData_shouldSaveAndRedirect() throws Exception {
        BookDto bookDto = createValidBookDto();

        mockMvc.perform(post("/books/save")
                        .with(csrf())
                        .flashAttr("book", bookDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        assertTrue(bookRepository.findByVendorCode(bookDto.getVendorCode()).isPresent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void saveBook_invalidData_shouldReturnFormWithErrors() throws Exception {
        BookDto bookDto = createValidBookDto();
        bookDto.setTitle(""); // Делаем невалидным

        mockMvc.perform(post("/books/save")
                        .with(csrf())
                        .flashAttr("book", bookDto))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeHasErrors("book"))
                .andExpect(model().attributeHasFieldErrors("book", "title"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showEditForm_admin_shouldReturnFormWithBook() throws Exception {
        Book book =  saveInDbValidTestBook("b-001", "Spring", "Manning", 2020);;

        mockMvc.perform(get("/books/edit/{id}", book.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeExists("book"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void showEditForm_nonAdmin_shouldForbidAccess() throws Exception {
        Book book =  saveInDbValidTestBook("b-001", "Spring", "Manning", 2020);

        mockMvc.perform(get("/books/edit/{id}", book.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateBook_validData_shouldUpdateAndRedirect() throws Exception {
        Book book =  saveInDbValidTestBook("b-001", "Spring", "Manning", 2020);
        BookDto bookDto = createValidBookDto();
        bookDto.setTitle("Updated Title");

        mockMvc.perform(post("/books/update/{id}", book.getId())
                        .with(csrf())
                        .flashAttr("book", bookDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        assertEquals("Updated Title", bookRepository.findById(book.getId()).get().getTitle());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteBook_admin_shouldDeleteAndRedirect() throws Exception {
        Book book =  saveInDbValidTestBook("b-001", "Spring", "Manning", 2020);

        mockMvc.perform(get("/books/delete/{id}", book.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        assertFalse(bookRepository.existsById(book.getId()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteBook_nonAdmin_shouldForbidAccess() throws Exception {
        Book book =  saveInDbValidTestBook("b-001", "Spring", "Manning", 2020);

        mockMvc.perform(get("/books/delete/{id}", book.getId()))
                .andExpect(status().isForbidden());
    }
}