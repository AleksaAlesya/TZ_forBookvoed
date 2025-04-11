package by.aleksabrakor.tzForBookvoed.mapper;

import by.aleksabrakor.tzForBookvoed.dto.BookDto;
import by.aleksabrakor.tzForBookvoed.entity.Book;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-11T13:20:46+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Amazon.com Inc.)"
)
@Component
public class BookMapperImpl implements BookMapper {

    @Override
    public Book dtoToEntity(BookDto bookDto) {
        if ( bookDto == null ) {
            return null;
        }

        Book book = new Book();

        book.setId( bookDto.getId() );
        book.setVendorCode( bookDto.getVendorCode() );
        book.setTitle( bookDto.getTitle() );
        book.setBrand( bookDto.getBrand() );
        book.setYear( bookDto.getYear() );
        book.setStock( bookDto.getStock() );
        book.setPrice( bookDto.getPrice() );

        return book;
    }

    @Override
    public BookDto entityToDto(Book book) {
        if ( book == null ) {
            return null;
        }

        BookDto bookDto = new BookDto();

        bookDto.setId( book.getId() );
        bookDto.setVendorCode( book.getVendorCode() );
        bookDto.setTitle( book.getTitle() );
        bookDto.setBrand( book.getBrand() );
        bookDto.setYear( book.getYear() );
        bookDto.setStock( book.getStock() );
        bookDto.setPrice( book.getPrice() );

        return bookDto;
    }

    @Override
    public List<BookDto> toDtoList(List<Book> books) {
        if ( books == null ) {
            return null;
        }

        List<BookDto> list = new ArrayList<BookDto>( books.size() );
        for ( Book book : books ) {
            list.add( entityToDto( book ) );
        }

        return list;
    }

    @Override
    public List<Book> toEntityList(List<BookDto> bookDtos) {
        if ( bookDtos == null ) {
            return null;
        }

        List<Book> list = new ArrayList<Book>( bookDtos.size() );
        for ( BookDto bookDto : bookDtos ) {
            list.add( dtoToEntity( bookDto ) );
        }

        return list;
    }
}
