package com.example.chartographerapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Min;

@Data
@Schema(description = "Позиция фрагмента чарты")
@Builder
public class ChartaFragmentDto {
//    @Min(0)
    @Schema(description = "Координата x", example = "0")
    private Integer x;

//    @Min(0)
    @Schema(description = "Координата y", example = "0")
    private Integer y;

    @Min(1)
    @Schema(description = "Ширина харты", example = "100")
    private Integer width;

    @Min(1)
    @Schema(description = "Высота харты", example = "100")
    private Integer height;
}
