package com.example.chartographerapp.controller;

import com.example.chartographerapp.exception.ChartaNotFoundException;
import com.example.chartographerapp.exception.FragmentNotCrossingChartaException;
import com.example.chartographerapp.service.ChartographerServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.IOException;

@RestController
@Validated
@RequestMapping("/chartas")
@Tag(name = "Контроллер http-запросов для харт (папирусов)",
        description = "Позволяет создавать харту, добавлять и получать фрагменты")
public class ChartoGrapherApiController {

    private final ChartographerServiceImpl service;

    @Autowired
    public ChartoGrapherApiController(ChartographerServiceImpl service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
            summary = "Создание новой харты",
            description = "Позволяет создать новую харту. " +
                    "Создаётся пустое изображение харты в формате .bmp и харта добавляется в бд."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Charta created"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Arguments are incorrect"
            )
    })
    public ResponseEntity<String> createCharta(@RequestParam("width") @Min(1) @Max(20_000) Integer width,
                                               @RequestParam("height") @Min(1) @Max(50_000) Integer height) throws IOException {
        return new ResponseEntity<>(service.createCharta(width, height), HttpStatus.CREATED);
    }

    @PostMapping(value = "/{id}", consumes = "image/bmp")
    @Operation(
            summary = "Сохранение фрагмента в Харту",
            description = "Позволяет сохранить фрагмент (изображение в формате bmp) в Харту"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Arguments are correct and function saved fragment of Charta"
                    ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Charta not found, may be id is incorrect"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Arguments are incorrect"
            )
    })
    public ResponseEntity<?> saveChartaFragment(@PathVariable Integer id,
                                   @RequestParam("x") @Min(0) Integer x,
                                   @RequestParam("y") @Min(0) Integer y,
                                   @RequestParam("width") @Min(1) Integer width,
                                   @RequestParam("height") @Min(1) Integer height,
                                   @RequestBody byte[] array) throws IOException, ChartaNotFoundException, FragmentNotCrossingChartaException {
        service.addFragmentInCharta(id, width, height, x, y, array);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = "image/bmp")
    @ResponseBody
    @Operation(
            summary = "Получение фрагмента Харты",
            description = "Позволяет получить фрагмент (изображение в формате bmp) Харты"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Arguments are correct and function found fragment of Charta",
                    content = {
                            @Content(mediaType = "image/bmp")
                    }),
            @ApiResponse(
                    responseCode = "404",
                    description = "Charta not found, may be id is incorrect"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Arguments are incorrect"
            )
    })
    public byte[] getCharta(@PathVariable Integer id,
                            @RequestParam("x") @Min(0) Integer x,
                            @RequestParam("y") @Min(0) Integer y,
                            @RequestParam("width") @Min(1) @Max(5000) Integer width,
                            @RequestParam("height") @Min(1) @Max(5000) Integer height) throws IOException, ChartaNotFoundException, FragmentNotCrossingChartaException {
        return service.getFragmentInCharta(id, x, y, width, height);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удаление Харты",
            description = "Позволяет удалить Харту целиком (как и изображение, так и данные из бд)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Charta deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Charta not found, may be id is incorrect"
            )
    })
    public ResponseEntity<?> deleteCharta(@PathVariable Integer id) throws ChartaNotFoundException {
        service.deleteCharta(id);
        return new ResponseEntity<String>(HttpStatus.OK);
    }
}
