package com.example.chartographerapp.exception.handler;

import com.example.chartographerapp.exception.ChartaNotFoundException;
import com.example.chartographerapp.exception.FragmentNotCrossingChartaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
@Slf4j
public class ErrorHandlingControllerAdvice {
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Void> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("ConstraintViolationException exception " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(ChartaNotFoundException.class)
    public ResponseEntity<Void> handleChartaNotFoundException(ChartaNotFoundException ex) {
        log.error("ChartaNotFoundException exception " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(FragmentNotCrossingChartaException.class)
    public ResponseEntity<Void> handleFragmentNotCrossingChartaException(FragmentNotCrossingChartaException ex) {
        log.error("FragmentNotCrossingChartaException exception " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Void> handleBindException(BindException ex) {
        log.error("BindException exception " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleError(Exception ex) {
        log.error("RuntimeException exception" + ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
