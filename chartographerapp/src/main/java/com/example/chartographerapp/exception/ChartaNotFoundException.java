package com.example.chartographerapp.exception;

public class ChartaNotFoundException extends Exception {
    public ChartaNotFoundException(Integer id) {
        super(String.format("Charta %d not found", id));
    }
}
