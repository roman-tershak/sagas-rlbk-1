package rt.sagas.order.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ErrorControllerAdvice {

    private static final Logger LOGGER = LogManager.getLogger();

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseBody
    public ResponseEntity<ApiError> handleException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        LOGGER.error(message);
        return getApiErrorResponseEntity(
                message,
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({Exception.class})
    @ResponseBody
    public ResponseEntity<ApiError> handleException(Exception e) {
        String message = "Internal server error - " + e.getLocalizedMessage();

        LOGGER.error(message, e);
        return getApiErrorResponseEntity(
                message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiError> getApiErrorResponseEntity(String errorMessage, HttpStatus status) {
        return new ResponseEntity<>(
                new ApiError(errorMessage), status);
    }

}
