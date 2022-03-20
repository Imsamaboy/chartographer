package com.example.chartographerapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
@Builder
public class CreateChartaDto {
    @Schema(description = "Ширина харты", example = "1000")
    @Range(min = 1, max = 20000)
    Integer width;

    @Schema(description = "Высота харты", example = "1000")
    @Range(min = 1, max = 50000)
    Integer height;
}
