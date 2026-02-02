package com.example.Task1.service;

import com.example.Task1.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface IServerService {
    void create(ServerRequestDto s) throws Exception;

    void create(MultipartFile file);


    ServerResponseDto getById(int id);

    List<ServerResponseDto> getAll();

    void update(ServerUpdateRequestDto sDto, int serverId);

    void delete(int id);


    ByteArrayInputStream download();
}

