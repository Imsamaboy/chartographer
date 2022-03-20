package com.example.chartographerapp;

import com.example.chartographerapp.controller.ChartoGrapherApiController;
import com.example.chartographerapp.dto.ChartaFragmentDto;
import com.example.chartographerapp.dto.CreateChartaDto;
import com.example.chartographerapp.dto.GetChartaDto;
import com.example.chartographerapp.exception.ChartaNotFoundException;
import com.example.chartographerapp.exception.FragmentNotCrossingChartaException;
import com.example.chartographerapp.service.api.ChartographerApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChartoGrapherApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChartoGrapherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    ChartographerApiService chartographerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGoodCreateRequests() throws Exception {
        // given
        CreateChartaDto createChartaDto = CreateChartaDto.builder()
                .height(100)
                .width(100)
                .build();

        when(chartographerService.createCharta(createChartaDto)).thenReturn("1");
        // when
        mockMvc.perform(post("/chartas")
                        .content(objectMapper.writeValueAsString(createChartaDto)))
                // then
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void testBadCreateRequests() throws Exception {
        // given
        Map<String, String> widthAndHeightValues = Map.of(
                "20001", "50001",
                "100000000", "500000000",
                "-100", "500",
                "100", "-500",
                "-10", "-500",
                "0", "0"
        );

        for (String width: widthAndHeightValues.keySet()) {
            // when
            mockMvc.perform(
                            post("/chartas")
                                    .param("width", width)
                                    .param("height", widthAndHeightValues.get(width))
                    )
                    // then
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void testSaveFragmentGoodRequest() throws Exception {
        // given
        byte[] array = new byte[] {13, 32};
        var id = 1;
        var chartaFragmentDto = ChartaFragmentDto.builder()
                .x(0)
                .y(0)
                .height(100)
                .width(100)
                .build();

        doNothing().when(chartographerService).addFragmentInCharta(id, chartaFragmentDto, array);

        // when
        mockMvc.perform(post("/chartas/1")
                        .content(objectMapper.writeValueAsString(chartaFragmentDto))
                        .content(array)
                        .contentType("image/bmp"))
                // then
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testSaveFragmentNotFound() throws Exception {
        // given
        byte[] array = new byte[] {13, 32};
        var chartaFragmentDto = ChartaFragmentDto.builder()
                .x(0)
                .y(0)
                .height(-100)
                .width(-100)
                .build();

        doThrow(ChartaNotFoundException.class)
                .when(chartographerService).addFragmentInCharta(anyInt(), any(ChartaFragmentDto.class), any(byte[].class));

        // when
        mockMvc.perform(post("/chartas/1")
                        .content(objectMapper.writeValueAsString(chartaFragmentDto))
                        .content(array)
                        .contentType("image/bmp"))
                // then
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testSaveFragmentNotCrossingException() throws Exception {
        // given
        byte[] array = new byte[] {13, 32};
        var chartaFragmentDto = ChartaFragmentDto.builder()
                .x(0)
                .y(0)
                .height(-100)
                .width(-100)
                .build();

        doThrow(FragmentNotCrossingChartaException.class)
                .when(chartographerService).addFragmentInCharta(anyInt(), any(ChartaFragmentDto.class), any(byte[].class));

        // when
        mockMvc.perform(post("/chartas/1")
                        .content(objectMapper.writeValueAsString(chartaFragmentDto))
                        .content(array)
                        .contentType("image/bmp"))
                // then
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetFragmentGoodRequest() throws Exception {
        // given
        GetChartaDto getChartaDto = GetChartaDto.builder()
                .x(0)
                .y(0)
                .width(100)
                .height(100)
                .build();
        byte[] bytes = new byte[] {123, 123, 123};
        when(chartographerService.getFragmentInCharta(1, getChartaDto)).thenReturn(bytes);

        mockMvc.perform(get("/chartas/" + "1")
                        .content(objectMapper.writeValueAsString(getChartaDto))
                        .contentType("image/bmp"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testGetFragmentNotFound() throws Exception {
        // given
        GetChartaDto getChartaDto = GetChartaDto.builder()
                .x(0)
                .y(0)
                .width(100)
                .height(100)
                .build();

        when(chartographerService.getFragmentInCharta(anyInt(), any(GetChartaDto.class)))
                .thenThrow(ChartaNotFoundException.class);

        mockMvc.perform(get("/chartas/1")
                        .content(objectMapper.writeValueAsString(getChartaDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetFragmentNotCrossing() throws Exception {
        // given
        GetChartaDto getChartaDto = GetChartaDto.builder()
                .x(0)
                .y(0)
                .width(100)
                .height(100)
                .build();
        byte[] bytes = new byte[] {123, 123, 123};
        when(chartographerService.getFragmentInCharta(anyInt(), any(GetChartaDto.class)))
                .thenThrow(FragmentNotCrossingChartaException.class);

        mockMvc.perform(get("/chartas/1")
                        .content(objectMapper.writeValueAsString(getChartaDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

}
