package by.aleksabrakor.tzForBookvoed.util;


import by.aleksabrakor.tzForBookvoed.dto.BookDto;
import by.aleksabrakor.tzForBookvoed.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class BookValidator implements Validator {
    private final BookService bookService;

    @Override
    public boolean supports(Class<?> clazz) {
        return BookDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        BookDto bookDto = (BookDto) target;

        if (bookService.findByVendorCode(bookDto.getVendorCode(), bookDto.getId()).isPresent()) {
            errors.rejectValue("VendorCode", "", "Этот VendorCode уже существует");
        }
    }
}
