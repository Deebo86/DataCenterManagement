package com.example.Task1.dto;

import com.example.Task1.model.enums.DataCenterType;
import com.example.Task1.validation.ValidCountry;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


//No ServerIds provided here
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataCenterRequestDto {
    @NotEmpty
    @ValidCountry
    private String country;
    @NotEmpty
    @Pattern(regexp = "^\\s*[^,]+\\s*,\\s*[^,]+\\s*,\\s*[^,]+\\s*$", message = "Address must contain three comma-separated parts")
    private String address;
    @Max(100)
    @Min(1)
    private int maxNoOfServers;
    private int currentNoOfServers;
    @NotNull
    private DataCenterType dataCenterType;
}
