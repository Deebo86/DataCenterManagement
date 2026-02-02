package com.example.Task1.dto;

import com.example.Task1.model.enums.DataCenterType;
import com.example.Task1.validation.ValidCountry;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataCenterUpdateRequestDto {
    @ValidCountry
    private String country;
    @Pattern(regexp = "^\\s*[^,]+\\s*,\\s*[^,]+\\s*,\\s*[^,]+\\s*$", message = "Address must contain three comma-separated parts")
    private String address;
    @Max(100)
    @Min(1)
    private Integer maxNoOfServers;
    private Integer currentNoOfServers;
    private DataCenterType dataCenterType;
}
