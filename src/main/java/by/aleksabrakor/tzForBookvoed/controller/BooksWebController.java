package by.aleksabrakor.tzForBookvoed.controller;


import by.aleksabrakor.tzForBookvoed.dto.BookDto;
import by.aleksabrakor.tzForBookvoed.service.BookService;
import by.aleksabrakor.tzForBookvoed.util.BookValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
@Log4j2
public class BooksWebController {

    private final BookService bookService;
    private  final BookValidator bookValidator;

    @GetMapping
    public String findAllBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            Principal principal) {

        log.info("Получение списка всех книг с учетом пагинации и фильтрации {}, {}, {}", title,brand, year);

        if (principal != null) {
            log.debug("Пользователь аутентифицирован: {}", principal.getName());
            model.addAttribute("username", principal.getName());
        }

        Page<BookDto> booksPage = bookService.findAllBooks(title, brand, year, page, size);
        model.addAttribute("books", booksPage);
        model.addAttribute("title", title);
        model.addAttribute("brand", brand);
        model.addAttribute("year", year);
        return "books/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String showCreateForm(Model model) {

        log.info("Запрос формы создания новой книги");
        model.addAttribute("book", new BookDto());
        return "books/form";
    }

    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String saveBook(@ModelAttribute("book") @Valid BookDto bookDto,
                           BindingResult bindingResult) {

        log.info("Попытка сохранения книги: {}", bookDto.getTitle());

        if (!processBook(bookDto, bindingResult)) {
            return "books/form";
        }
        BookDto savedBook = bookService.saveBook(bookDto);
        log.info("Книга успешно сохранена. ID: {}", savedBook.getId());
        return "redirect:/books";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Long id,
                               Model model) {

        log.info("Запрос формы редактирования книги. ID: {}", id);
        model.addAttribute("book", bookService.findBookById(id));
        return "books/form";
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateBook(@PathVariable Long id,
                             @ModelAttribute("book") BookDto bookDto,
                             BindingResult bindingResult) {

        log.info("Попытка обновления книги. ID: {}", id);
        if (!processBook(bookDto, bindingResult)) {
            return "books/form";
        }
        BookDto updatedBook = bookService.updateBook(id, bookDto);
        log.info("Книга успешно обновлена. ID: {}", updatedBook.getId());
        return "redirect:/books";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteBook(@PathVariable Long id) {
        log.info("Запрос на  удаления книги. ID: {}", id);

        bookService.deleteBook(id);
        return "redirect:/books";
    }

    private boolean processBook(BookDto bookDto, BindingResult bindingResult){

        log.info("Валидация ..");
        bookValidator.validate(bookDto, bindingResult);

        if (bindingResult.hasErrors()) {
            log.error("Validation errors: {}", bindingResult.getAllErrors());
            return false;
        }
        return true;
    }
}
