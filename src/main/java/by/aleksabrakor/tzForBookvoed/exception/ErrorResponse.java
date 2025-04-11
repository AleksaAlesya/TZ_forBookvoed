package by.aleksabrakor.tzForBookvoed.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private String message;

    private Timestamp timestamp;
}