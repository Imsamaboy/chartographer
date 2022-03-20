package com.example.chartographerapp.controller;

import com.example.chartographerapp.dto.ChartaFragmentDto;
import com.example.chartographerapp.dto.CreateChartaDto;
import com.example.chartographerapp.dto.GetChartaDto;
import com.example.chartographerapp.exception.ChartaNotFoundException;
import com.example.chartographerapp.exception.FragmentNotCrossingChartaException;
import com.example.chartographerapp.service.api.ChartographerApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

@RestController
//@Validated
@RequestMapping("/chartas")
@Tag(name = "Контроллер http-запросов для харт (папирусов)",
        description = "Позволяет создавать харту, добавлять и получать фрагменты")
//@RequiredArgsConstructor
public class ChartoGrapherApiController {

    private final ChartographerApiService service;

    @Autowired
    public ChartoGrapherApiController(ChartographerApiService service) {
        this.service = service;
    }

    @Operation(
            summary = "Создание новой харты",
            description = "Позволяет создать новую харту. " +
                    "Создаётся пустое изображение харты в формате .bmp и харта добавляется в бд."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Charta created"),
            @ApiResponse(responseCode = "400", description = "Arguments are incorrect", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content)
    })
    @Parameter(name = "createChartaDto", hidden = true)
    @Parameter(name = "width", in = ParameterIn.QUERY, schema = @Schema(type = "integer", description = "Ширина"))
    @Parameter(name = "height", in = ParameterIn.QUERY, schema = @Schema(type = "integer", description = "Высота"))
    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public String createCharta(@Valid CreateChartaDto createChartaDto) throws IOException {
        return service.createCharta(createChartaDto);
    }

    @Operation(
            summary = "Сохранение фрагмента в Харту",
            description = "Позволяет сохранить фрагмент (изображение в формате bmp) в Харту"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Arguments are correct and function saved fragment of Charta"),
            @ApiResponse(responseCode = "404", description = "Charta not found, may be id is incorrect", content = @Content),
            @ApiResponse(responseCode = "400", description = "Arguments are incorrect", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content)
    })
    @Parameter(name = "chartaFragmentDto", hidden = true)
    @Parameter(name = "width", in = ParameterIn.QUERY, schema = @Schema(type = "integer", description = "Ширина"))
    @Parameter(name = "height", in = ParameterIn.QUERY, schema = @Schema(type = "integer", description = "Высота"))
    @Parameter(name = "x", in = ParameterIn.QUERY, schema = @Schema(type = "integer", description = "Координата по оси x"))
    @Parameter(name = "y", in = ParameterIn.QUERY, schema = @Schema(type = "integer", description = "Координата по оси y"))
    @PostMapping(value = "/{id}", consumes = {"image/bmp", MediaType.APPLICATION_JSON_VALUE, "image/bmp;charset=UTF-8"})
    public void saveChartaFragment(@PathVariable Integer id,
                                   @Valid ChartaFragmentDto chartaFragmentDto,
                                   @RequestBody byte[] array) throws IOException, ChartaNotFoundException, FragmentNotCrossingChartaException {
        service.addFragmentInCharta(id, chartaFragmentDto, array);
    }

    @Operation(
            summary = "Получение фрагмента Харты",
            description = "Позволяет получить фрагмент (изображение в формате bmp) Харты"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Arguments are correct and function found fragment of Charta",
                    content = {@Content(mediaType = "image/bmp")}),
            @ApiResponse(responseCode = "404", description = "Charta not found, may be id is incorrect", content = @Content),
            @ApiResponse(responseCode = "400", description = "Arguments are incorrect", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content)
    })
    @Parameter(name = "getChartaDto", hidden = true)
    @Parameter(name = "width", in = ParameterIn.QUERY, schema = @Schema(type = "integer", description = "Ширина"))
    @Parameter(name = "height", in = ParameterIn.QUERY, schema = @Schema(type = "integer", description = "Высота"))
    @Parameter(name = "x", in = ParameterIn.QUERY, schema = @Schema(type = "integer", description = "Координата по оси x"))
    @Parameter(name = "y", in = ParameterIn.QUERY, schema = @Schema(type = "integer", description = "Координата по оси y"))
    @GetMapping(value = "/{id}", produces = "image/bmp")
    @ResponseBody
    public byte[] getCharta(@PathVariable Integer id, @Valid GetChartaDto getChartaDto) throws IOException,
            ChartaNotFoundException, FragmentNotCrossingChartaException {
        return service.getFragmentInCharta(id, getChartaDto);
    }

    @Operation(
            summary = "Удаление Харты",
            description = "Позволяет удалить Харту целиком (как и изображение, так и данные из бд)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Charta deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Charta not found, may be id is incorrect", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public void deleteCharta(@PathVariable Integer id) throws ChartaNotFoundException {
        service.deleteCharta(id);
    }
}
