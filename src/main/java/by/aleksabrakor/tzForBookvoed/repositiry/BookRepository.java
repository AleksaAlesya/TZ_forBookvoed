package by.aleksabrakor.tzForBookvoed.repositiry;

import by.aleksabrakor.tzForBookvoed.entity.Book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT b FROM Book b WHERE " +
            "(:title IS NULL OR b.title ILIKE %:title%) AND " +
            "(:brand IS NULL OR b.brand ILIKE %:brand%) AND " +
            "(:year IS NULL OR b.year = :year)")
    Page<Book> findByFilters(
            @Param("title") String title,
            @Param("brand") String brand,
            @Param("year") Integer year,
            Pageable pageable);

    Optional<Book> findByVendorCodeAndIdNot(String vendorCode, Long id);

    Optional<Book> findByVendorCode(String vendorCode);
}

