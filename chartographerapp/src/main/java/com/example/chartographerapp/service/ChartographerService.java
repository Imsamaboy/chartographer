package com.example.chartographerapp.service;

import com.example.chartographerapp.exception.ChartaNotFoundException;
import com.example.chartographerapp.exception.FragmentNotCrossingChartaException;
import org.springframework.stereotype.Service;

import java.io.IOException;

public interface ChartographerService {
    String createCharta(Integer width, Integer height) throws IOException;

    void deleteCharta(Integer id) throws ChartaNotFoundException;

    void addFragmentInCharta(Integer id, Integer width, Integer height, Integer x, Integer y, byte[] array) throws IOException, ChartaNotFoundException, FragmentNotCrossingChartaException;

    byte[] getFragmentInCharta(Integer id, Integer x, Integer y, Integer width, Integer height) throws IOException, ChartaNotFoundException, FragmentNotCrossingChartaException;
}
