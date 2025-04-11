package by.aleksabrakor.tzForBookvoed.controller;


import by.aleksabrakor.tzForBookvoed.dto.BookDto;
import by.aleksabrakor.tzForBookvoed.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/books")
@RequiredArgsConstructor
@Slf4j
public class BooksRestController {

    private final BookService bookService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<BookDto> getAllBooks() {
        log.info("GET api/books— получение списка всех книг");
        return bookService.findAllBooks();
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public BookDto findBookById(@PathVariable Long id) {
        log.info("GET api/books//{id} — получение книги по ID");
        return bookService.findBookById(id);
    }

    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
    public BookDto createBook(@RequestBody @Valid BookDto bookDto) {
        log.info("POST api/books — добавление новой книги");
        return bookService.saveBook(bookDto);
    }

    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public BookDto updateBook(@PathVariable Long id, @RequestBody BookDto bookDto) {
        log.info("PUT api/books//{id} — редактирование книги по ID");
        return bookService.updateBook(id, bookDto);
    }

    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        log.info("ВУДУЕУ api/books//{id} — удаление книги по ID");
        bookService.deleteBook(id);
        return ResponseEntity.ok().build();
    }
}
