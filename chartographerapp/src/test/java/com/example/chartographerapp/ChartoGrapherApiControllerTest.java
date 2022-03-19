package com.example.chartographerapp;

import com.example.chartographerapp.controller.ChartoGrapherApiController;
import com.example.chartographerapp.entity.Charta;
import com.example.chartographerapp.entity.Fragment;
import com.example.chartographerapp.model.FragmentModel;
import com.example.chartographerapp.repository.ChartoRepository;
import com.example.chartographerapp.repository.FragmentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
//@WebMvcTest(controllers = ChartoGrapherApiController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ChartoGrapherApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
//    @MockBean
    private ChartoGrapherApiController chartoGrapherApiController;

    @Autowired
    private ChartoRepository chartoRepository;

    @Autowired
    private FragmentRepository fragmentRepository;

    @Test
    void testGoodCreateRequests() throws Exception {
        Map<String, String> widthAndHeightValues = Map.of(
                "500", "500",
                "1000", "500",
                "20000", "50000",
                "100", "100",
                "1", "1",
                "25", "25",
                "4000", "6000"
        );

        List<String> ids = new ArrayList<>();
        for (String width: widthAndHeightValues.keySet()) {

//            when(chartoGrapherApiController.createCharta(Integer.valueOf(width),
//                    Integer.valueOf(widthAndHeightValues.get(width))))
//                    .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

            ids.add(this.mockMvc.perform(
                    post("/chartas")
                    .param("width", width)
                    .param("height", widthAndHeightValues.get(width))
                    )
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString());

//            verify(chartoGrapherApiController).createCharta(Integer.valueOf(width),
//                    Integer.valueOf(widthAndHeightValues.get(width)));
        }

        ids.stream()
                .map(id -> chartoRepository.getChartaById(Integer.valueOf(id)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(charta -> fragmentRepository.findFragmentByCharta(charta))
                .flatMap(Collection::stream)
                .forEach(fragment -> new File(fragment.getFileName()).delete());
    }

    @Test
    void testBadCreateRequests() throws Exception {
        Map<String, String> widthAndHeightValues = Map.of(
                "20001", "50001",
                "100000000", "500000000",
                "-100", "500",
                "100", "-500",
                "-10", "-500",
                "0", "0"
        );
        for (String width: widthAndHeightValues.keySet()) {
            this.mockMvc.perform(
                            post("/chartas")
                            .param("width", width)
                            .param("height", widthAndHeightValues.get(width))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void testSaveFragmentGoodRequest() throws Exception {
        // Добавим тестовую харту
        Charta charta = new Charta(-1, 200, 200, null);
        chartoRepository.save(charta);
        Fragment fragment = new Fragment(-1, 0, 0, 200, 200,
                "src/main/resources/static/charta-200-200.bmp", charta);
        charta.setFragmentList(List.of(fragment));
        fragmentRepository.save(fragment);
        // Пути к тестовым файлам
        List<String> paths = List.of(
                "src/main/resources/static/test-fragment-50-50.bmp",
                "src/main/resources/static/test-fragment-70-70.bmp",
                "src/main/resources/static/empty-200-200.bmp"
        );
        // Создаем массивы байт
        List<byte[]> fragmentsAsByteArray = new ArrayList<>();
        for (String path: paths) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(ImageIO.read(new File(path)), "bmp", stream);
            fragmentsAsByteArray.add(stream.toByteArray());
        }
        // Тестовый кейсы на 50x50px
        List<FragmentModel> fragmentsWith50 = List.of(
                new FragmentModel("0", "0", "50", "50"),
                new FragmentModel("199", "199", "50", "50"),
                new FragmentModel("199", "0", "50", "50"),
                new FragmentModel("0", "199", "50", "50"),
                new FragmentModel("100", "100", "50", "50"),
                new FragmentModel("190", "100", "50", "50"),
                new FragmentModel("190", "190", "50", "50"),
                new FragmentModel("190", "190", "10", "10"),
                new FragmentModel("100", "190", "10", "10")
        );

        List<FragmentModel> fragmentsWith70 = List.of(
                new FragmentModel("0", "0", "70", "70"),
                new FragmentModel("199", "199", "70", "70"),
                new FragmentModel("199", "0", "70", "70"),
                new FragmentModel("0", "199", "70", "70"),
                new FragmentModel("120", "120", "70", "70"),
                new FragmentModel("190", "100", "70", "70"),
                new FragmentModel("190", "190", "70", "70"),
                new FragmentModel("190", "190", "70", "10"),
                new FragmentModel("100", "190", "10", "70")
        );

        List<FragmentModel> fragmentsWith200 = List.of(
                new FragmentModel("0", "0", "200", "200")
        );

        List<List<FragmentModel>> list = List.of(fragmentsWith50, fragmentsWith70, fragmentsWith200);
        // Делаем запросы и проверяем работу контроллера
        int index = 0;
        for (List<FragmentModel> currentList: list) {
            for (FragmentModel fragmentModel: currentList) {
                // Save Request
                this.mockMvc.perform(
                                post("/chartas/" + "-1")
                                        .param("x", fragmentModel.getX())
                                        .param("y", fragmentModel.getY())
                                        .param("width", fragmentModel.getWidth())
                                        .param("height", fragmentModel.getHeight())
                                        .content(fragmentsAsByteArray.get(index))
                                        .contentType("image/bmp"))
                        .andDo(print())
                        .andExpect(status().isOk());
            }
            index++;
        }
    }

    @Test
    void testSaveFragmentBadRequest() throws Exception {
        // Добавим тестовую харту
        Charta charta = new Charta(-2, 200, 200, null);
        chartoRepository.save(charta);
        Fragment fragment = new Fragment(-2, 0, 0, 200, 200,
                "src/main/resources/static/charta-200-200.bmp", charta);
        charta.setFragmentList(List.of(fragment));
        fragmentRepository.save(fragment);

        // Пути к тестовым файлам
        List<String> paths = List.of(
                "src/main/resources/static/test-fragment-50-50.bmp",
                "src/main/resources/static/test-fragment-70-70.bmp",
                "src/main/resources/static/empty-200-200.bmp"
        );

        // Создаем массивы байт
        List<byte[]> fragmentsAsByteArray = new ArrayList<>();
        for (String path: paths) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(ImageIO.read(new File(path)), "bmp", stream);
            fragmentsAsByteArray.add(stream.toByteArray());
        }

        // Тестовый кейсы
        List<FragmentModel> fragmentsWithBadParams = List.of(
                new FragmentModel("-1", "50", "50", "50"),
                new FragmentModel("50", "-1", "50", "50"),
                new FragmentModel("-1", "-1", "50", "50"),
                new FragmentModel("200", "200", "50", "50"),
                new FragmentModel("0", "200", "50", "50"),
                new FragmentModel("200", "0", "50", "50"),
                new FragmentModel("200", "200", "50", "50"),
                new FragmentModel("50", "50", "0", "0"),
                new FragmentModel("50", "50", "-1", "10"),
                new FragmentModel("50", "50", "10", "-1"),
                new FragmentModel("50", "50", "-100", "-100"),
                new FragmentModel("10000", "10000", "50", "50")
        );

        List<List<FragmentModel>> list = List.of(fragmentsWithBadParams);
        // Делаем запросы и проверяем работу контроллера
        int index = 0;
        for (List<FragmentModel> currentList: list) {
            for (FragmentModel fragmentModel: currentList) {
                // Save Request
                this.mockMvc.perform(
                                post("/chartas/" + "-2")
                                        .param("x", fragmentModel.getX())
                                        .param("y", fragmentModel.getY())
                                        .param("width", fragmentModel.getWidth())
                                        .param("height", fragmentModel.getHeight())
                                        .content(fragmentsAsByteArray.get(index))
                                        .contentType("image/bmp"))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
            }
            index++;
        }
    }

    @Test
    void testGetFragmentGoodRequest() throws Exception {
        // Добавим тестовую харту
        Charta charta = new Charta(-3, 200, 200, null);
        chartoRepository.save(charta);
        Fragment fragment = new Fragment(-3, 0, 0, 200, 200,
                "src/main/resources/static/charta-200-200.bmp", charta);
        charta.setFragmentList(List.of(fragment));
        fragmentRepository.save(fragment);

        // Тестовый кейсы
        List<FragmentModel> fragmentsWithGoodParams = List.of(
                new FragmentModel("0", "0", "50", "50"),
                new FragmentModel("25", "25", "50", "50"),
                new FragmentModel("185", "185", "15", "15"),
                new FragmentModel("185", "185", "50", "50"),
                new FragmentModel("185", "50", "200", "200"),
                new FragmentModel("0", "199", "1", "1"),
                new FragmentModel("0", "199", "10", "10"),
                new FragmentModel("150", "190", "100", "100"),
                new FragmentModel("190", "190", "50", "100")
        );

        List<List<FragmentModel>> list = List.of(fragmentsWithGoodParams);
        // Делаем запросы и проверяем работу контроллера
        for (List<FragmentModel> currentList: list) {
            for (FragmentModel fragmentModel: currentList) {
                // Save Request
                this.mockMvc.perform(
                                get("/chartas/" + "-3")
                                        .param("x", fragmentModel.getX())
                                        .param("y", fragmentModel.getY())
                                        .param("width", fragmentModel.getWidth())
                                        .param("height", fragmentModel.getHeight()))
                        .andDo(print())
                        .andExpect(status().isOk());
            }
        }
    }

    @Test
    void testGetFragmentBadRequest() throws Exception {
        // Добавим тестовую харту
        Charta charta = new Charta(-4, 200, 200, null);
        chartoRepository.save(charta);
        Fragment fragment = new Fragment(-4, 0, 0, 200, 200,
                "src/main/resources/static/charta-200-200.bmp", charta);
        charta.setFragmentList(List.of(fragment));
        fragmentRepository.save(fragment);

        // Тестовый кейсы
        List<FragmentModel> fragmentsWithBadParams = List.of(
                new FragmentModel("-1", "0", "50", "50"),
                new FragmentModel("0", "-1", "50", "50"),
                new FragmentModel("-1", "-1", "50", "50"),
                new FragmentModel("200", "200", "50", "50"),
                new FragmentModel("500", "500", "50", "50"),
                new FragmentModel("150", "500", "50", "50"),
                new FragmentModel("500", "150", "50", "50"),
                new FragmentModel("100", "100", "-100", "50"),
                new FragmentModel("100", "100", "50", "-100"),
                new FragmentModel("100", "100", "-10", "-10"),
                new FragmentModel("100", "100", "5001", "5001")
        );

        List<List<FragmentModel>> list = List.of(fragmentsWithBadParams);
        // Делаем запросы и проверяем работу контроллера
        for (List<FragmentModel> currentList: list) {
            for (FragmentModel fragmentModel: currentList) {
                // Save Request
                this.mockMvc.perform(
                                get("/chartas/" + "-4")
                                        .param("x", fragmentModel.getX())
                                        .param("y", fragmentModel.getY())
                                        .param("width", fragmentModel.getWidth())
                                        .param("height", fragmentModel.getHeight()))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
            }
        }
    }
}
