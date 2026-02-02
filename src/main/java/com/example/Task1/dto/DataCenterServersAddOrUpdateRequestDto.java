package com.example.Task1.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataCenterServersAddOrUpdateRequestDto {
    @NotNull
    @Size(min = 1)
    private List<Integer> serverIds;
}
