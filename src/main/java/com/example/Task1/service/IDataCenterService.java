package com.example.Task1.service;

import com.example.Task1.dto.*;

import java.util.List;

public interface IDataCenterService {
    DataCenterResponseDto getById(int id);

    List<DataCenterResponseDto> getAll();

    void create(DataCenterRequestDto dc);

    void delete(int id);

    void update(DataCenterUpdateRequestDto dcDto, int id);

    void addOrUpdateServers(DataCenterServersAddOrUpdateRequestDto servers, int dataCenterId);

    List<DataCenterSummarizedDto> getAllSummarized();
}
