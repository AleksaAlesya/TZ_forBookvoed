package by.aleksabrakor.tzForBookvoed.service;

import by.aleksabrakor.tzForBookvoed.dto.BookDto;
import by.aleksabrakor.tzForBookvoed.entity.Book;
import by.aleksabrakor.tzForBookvoed.exception.NotCreatedException;
import by.aleksabrakor.tzForBookvoed.exception.NotFoundException;
import by.aleksabrakor.tzForBookvoed.mapper.BookMapper;
import by.aleksabrakor.tzForBookvoed.repositiry.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookDto findBookById(Long bookId) {
        return bookMapper.entityToDto(findBookOrThrow(bookId));
    }

    public List<BookDto> findAllBooks() {
        return bookMapper.toDtoList(bookRepository.findAll());
    }

    public Page<BookDto> findAllBooks(String title, String brand, Integer year, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title"));
        return bookMapper.toDtoPage(bookRepository.findByFilters(title, brand, year, pageable));
    }

    @Transactional
    public BookDto saveBook(BookDto bookDto) {
        if (bookRepository.findByVendorCode(bookDto.getVendorCode()).isPresent()) {
            throw new NotCreatedException("Книга с таким VendorCode уже существует");
        }
        return bookMapper.entityToDto(bookRepository.save(bookMapper.dtoToEntity(bookDto)));
    }

    @Transactional
    public BookDto updateBook(Long bookId, BookDto bookDto) {
        Book book = findBookOrThrow(bookId);
        if (bookRepository.findByVendorCodeAndIdNot(bookDto.getVendorCode(), bookId).isPresent()) {
            throw new NotCreatedException("Книга с таким VendorCode уже существует");
        }

        if (bookDto.getVendorCode() != null && !bookDto.getVendorCode().equals(book.getVendorCode())) {
            book.setVendorCode(bookDto.getVendorCode());
        }
        if (bookDto.getTitle() != null && !bookDto.getTitle().equals(book.getTitle())) {
            book.setTitle(bookDto.getTitle());
        }
        if (bookDto.getBrand() != null && !bookDto.getBrand().equals(book.getBrand())) {
            book.setBrand(bookDto.getBrand());
        }
        if (bookDto.getYear() != null && !bookDto.getYear().equals(book.getYear())) {
            book.setYear(bookDto.getYear());
        }
        if (bookDto.getStock() != null && !bookDto.getStock().equals(book.getStock())) {
            book.setStock(bookDto.getStock());
        }
        if (bookDto.getPrice() != null && !bookDto.getPrice().equals(book.getPrice())) {
            book.setPrice(bookDto.getPrice());
        }

        log.info("Book updated: {}", book);
        return bookMapper.entityToDto(book);
    }

    @Transactional
    public void deleteBook(Long bookId) {

        bookRepository.delete(findBookOrThrow(bookId));
    }

    private Book findBookOrThrow(Long bookId) {
        return bookRepository.findById(bookId).orElseThrow(() -> new NotFoundException("Книга с id: " + bookId + " не найдена."));
    }

    //Для валидации уникальности VendorCode
    public Optional<Book> findByVendorCode(String vendorCode, Long id) {
        return bookRepository.findByVendorCodeAndIdNot(vendorCode, id).stream().findAny();
    }

}
