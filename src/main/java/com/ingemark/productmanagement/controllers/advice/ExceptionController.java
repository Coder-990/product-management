package com.ingemark.productmanagement.controllers.advice;

import com.ingemark.productmanagement.exceptions.InvalidDataException;
import com.ingemark.productmanagement.exceptions.NotFoundException;
import com.ingemark.productmanagement.exceptions.PlatformException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.util.ObjectUtils;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler({MethodArgumentNotValidException.class, HandlerMethodValidationException.class})
    public ProblemDetail handleValidationException(ErrorResponse errorResponse) {
        var detailMessageArguments = errorResponse.getDetailMessageArguments();
        var errorResponseMessage = Objects.isNull(detailMessageArguments) ?
                "validation exception" : Stream.of(detailMessageArguments)
                .filter(Predicate.not(ObjectUtils::isEmpty))
                .map(Object::toString)
                .collect(Collectors.joining(" ."));
        log.info("Validation error : {}", errorResponseMessage);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorResponseMessage);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.info("Data integrity violation exception : {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Data integrity violation exception");
    }

    @ExceptionHandler(InvalidDataException.class)
    public ProblemDetail handleInvalidDataException(InvalidDataException ex) {
        log.info("Invalid data exception : {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFoundException(NotFoundException ex) {
        log.info("Not found exception : {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PlatformException.class)
    public ProblemDetail handlePlatformException(PlatformException ex) {
        log.error("Platform exception error: ", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getDisplayMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception ex) {
        log.error("Internal Server error : ", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
