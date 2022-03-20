package com.example.chartographerapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;

@Data
@Builder
public class GetChartaDto {
//    @Min(0)
    @Schema(description = "Координата x", example = "0")
    private Integer x;

//    @Min(0)
    @Schema(description = "Координата y", example = "0")
    private Integer y;

    @Range(min = 1, max = 5000)
    @Schema(description = "Ширина харты", example = "100")
    private Integer width;

    @Range(min = 1, max = 5000)
    @Schema(description = "Высота харты", example = "100")
    private Integer height;
}
