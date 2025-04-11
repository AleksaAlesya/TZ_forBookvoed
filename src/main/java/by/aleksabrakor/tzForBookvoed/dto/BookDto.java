package by.aleksabrakor.tzForBookvoed.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookDto {

    private Long id;

    @NotBlank(message = "Поле vendorCode не должно быть пустым")
    @Size(min=1, max =50)
    private String vendorCode;

    @NotBlank(message = "Поле title не должно быть пустым")
    @Size(min=1, max =200)
    private String title;

    @NotBlank(message = "Поле brand не должно быть пустым")
    @Size(min=1, max =50)
    private String brand;

    @NotNull
    private Integer year;

    private Integer stock;

    private BigDecimal price;

}