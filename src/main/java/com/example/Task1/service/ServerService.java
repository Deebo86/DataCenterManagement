package com.example.Task1.service;

import com.example.Task1.dto.*;
import com.example.Task1.exception.FileProcessingException;
import com.example.Task1.exception.UploadedEmptyFileException;
import com.example.Task1.exception.ServerNotFoundException;
import com.example.Task1.mapper.IServerMapper;
import com.example.Task1.model.Server;
import com.example.Task1.model.enums.OperatingSystem;
import com.example.Task1.repository.DataCenterRepo;
import com.example.Task1.repository.ServerRepo;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServerService implements IServerService {

    @Autowired
    private ServerRepo serverRepo;
    @Autowired
    private IServerMapper serverMapper;
    @Autowired
    private DataCenterRepo dcRepo;

    @Override
    @Transactional
    public void create(ServerRequestDto s) throws Exception {
        Server mapped = serverMapper.toServer(s);
        serverRepo.save(mapped);
    }

    @Override
    public void create(MultipartFile file) {
        if (file.isEmpty())
            throw new UploadedEmptyFileException("File uploaded is empty. Please upload a file containing data");

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row r : sheet)
            {
                if(r.getRowNum() == 0) continue;

                Server s = new Server();
                s.setMemory((int) r.getCell(0).getNumericCellValue());
                s.setNoOfCPU((int) r.getCell(1).getNumericCellValue());
                String os = r.getCell(2).getStringCellValue();
                s.setOs(OperatingSystem.valueOf(os));

                serverRepo.save(s);

            }
        } catch (IOException e) {
            throw new FileProcessingException(e.getMessage());
        }
    }

    @Override
    public ServerResponseDto getById(int serverId) {
        Server s = serverRepo.findById(serverId).orElseThrow(() -> new ServerNotFoundException("Server with id " + serverId + " does not exist."));
        ServerResponseDto responseDto = serverMapper.toResponseDto(s);
        if (s.getDatacenter() == null)
            responseDto.setDatacenterId(-1);
        return responseDto;
    }

    @Override
    public List<ServerResponseDto> getAll() {
        List<ServerResponseDto> dtos = new ArrayList<>();
        List<Server> servers = serverRepo.findAll();
        servers.forEach(server -> {
            ServerResponseDto responseDto = serverMapper.toResponseDto(server);
            if (server.getDatacenter() == null)
                responseDto.setDatacenterId(-1);
            dtos.add(responseDto);
        });
        return dtos;
    }

    @Override
    @Transactional
    public void update(ServerUpdateRequestDto sDto, int serverId) {
        Server server = this.serverRepo.findById(serverId).orElseThrow(() -> new ServerNotFoundException("Server with id " + serverId + " does not exist."));
        serverMapper.updateServerFromDto(sDto, server);
        this.serverRepo.save(server);
    }

    @Override
    @Transactional
    public void delete(int serverId) {
        if (serverRepo.existsById(serverId))
            serverRepo.deleteById(serverId);
        else
            throw new ServerNotFoundException("Server with id " + serverId + " does not exist.");
    }

    @Override
    public ByteArrayInputStream download() {
        List<ServerResponseDto> serverDtos = this.getAll();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try(Workbook workbook = new XSSFWorkbook())
        {
            Sheet sheet = workbook.createSheet("ServerData");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Id");
            header.createCell(1).setCellValue("DataCenter Id");
            header.createCell(2).setCellValue("Memory");
            header.createCell(3).setCellValue("NoOfCPUs");
            header.createCell(4).setCellValue("OS");


            int rowIndex = 1;

            for (ServerResponseDto s : serverDtos)
            {
                Row r = sheet.createRow(rowIndex++);
                r.createCell(0).setCellValue(s.getId());
                r.createCell(1).setCellValue(s.getDatacenterId());
                r.createCell(2).setCellValue(s.getMemory());
                r.createCell(3).setCellValue(s.getNoOfCPU());
                r.createCell(4).setCellValue(s.getOs().toString());
            }


            workbook.write(out);

        } catch (IOException e) {
            throw new FileProcessingException(e.getMessage());
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
