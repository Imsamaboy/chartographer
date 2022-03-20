package com.example.chartographerapp.service.api;

import com.example.chartographerapp.dto.ChartaFragmentDto;
import com.example.chartographerapp.dto.CreateChartaDto;
import com.example.chartographerapp.dto.GetChartaDto;
import com.example.chartographerapp.exception.ChartaNotFoundException;
import com.example.chartographerapp.exception.FragmentNotCrossingChartaException;
import com.example.chartographerapp.service.ChartographerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChartographerApiService {
    private final ChartographerService chartographerService;

    public String createCharta(CreateChartaDto createChartaDto) throws IOException {
        String chartaId = chartographerService.createCharta(createChartaDto);
        log.info(String.format("Charta %s was created.", chartaId));
        return chartaId;
    }

    public void deleteCharta(Integer id) throws ChartaNotFoundException {
        chartographerService.deleteCharta(id);
        log.info(String.format("Charta %d was deleted.", id));
    }

    public void addFragmentInCharta(Integer id, ChartaFragmentDto chartaFragmentDto, byte[] array) throws IOException,
            ChartaNotFoundException, FragmentNotCrossingChartaException {
        chartographerService.addFragmentInCharta(id, chartaFragmentDto, array);
        log.info(String.format("Charta %d fragment was added", id));
    }

    public byte[] getFragmentInCharta(Integer id, GetChartaDto chartaDto) throws IOException,
            ChartaNotFoundException, FragmentNotCrossingChartaException {
        byte[] fragments = chartographerService.getFragmentInCharta(id, chartaDto);
        log.info(String.format("Getting fragment from charta %d", id));
        return fragments;
    }
}
