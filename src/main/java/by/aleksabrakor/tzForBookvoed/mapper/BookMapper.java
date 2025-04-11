package by.aleksabrakor.tzForBookvoed.mapper;


import by.aleksabrakor.tzForBookvoed.dto.BookDto;
import by.aleksabrakor.tzForBookvoed.entity.Book;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookMapper {


    Book dtoToEntity(BookDto bookDto);


    BookDto entityToDto(Book book);

    List<BookDto> toDtoList(List<Book> books);

    List<Book> toEntityList(List<BookDto> bookDtos);

    default Page<BookDto> toDtoPage(Page<Book> bookPage) {
        return bookPage.map(this::entityToDto);
    }
}
