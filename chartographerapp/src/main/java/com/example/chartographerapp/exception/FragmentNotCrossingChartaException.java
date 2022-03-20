package com.example.chartographerapp.exception;

public class FragmentNotCrossingChartaException extends Exception {
    public FragmentNotCrossingChartaException(Integer x, Integer y) {
        super(String.format("Fragment with this coordinates %d %d is not crossing charta", x, y));
    }
}
