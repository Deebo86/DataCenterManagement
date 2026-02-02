package com.example.Task1.dto;

import com.example.Task1.model.enums.DataCenterType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataCenterResponseDto {
    private int id;
    private String country;
    private String address;
    private int maxNoOfServers;
    private int currentNoOfServers;
    private List<Integer> serverIds;
    private DataCenterType dataCenterType;
}


