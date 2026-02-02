package com.example.Task1.service;

import com.example.Task1.dto.*;
import com.example.Task1.exception.CurrentCapacityOverMaxCapacityException;
import com.example.Task1.exception.DataCenterNotFoundException;
import com.example.Task1.exception.NotAllServersFoundException;
import com.example.Task1.mapper.IDataCenterMapper;
import com.example.Task1.model.DataCenter;
import com.example.Task1.model.Server;
import com.example.Task1.repository.DataCenterRepo;
import com.example.Task1.repository.ServerRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DataCenterService implements IDataCenterService {
    @Autowired
    private DataCenterRepo dataCenterRepo;
    @Autowired
    private IDataCenterMapper dataCenterMapper;
    @Autowired
    private ServerRepo serverRepo;

    @Override
    public DataCenterResponseDto getById(int dataCenterId) {
        return dataCenterMapper
                .toDataCenterResponseDto
                        (dataCenterRepo
                                .findById(dataCenterId)
                                .orElseThrow(() -> new DataCenterNotFoundException("Datacenter with id " + dataCenterId + " does not exist.")));
    }

    @Override
    public List<DataCenterResponseDto> getAll() {
        List<DataCenter> dcList = dataCenterRepo.findAll();
        if (dcList.isEmpty())
            return new ArrayList<>();
        return dataCenterMapper
                .toDataCenterResponseDto(
                        dcList
                );
    }

    @Override
    @Transactional
    public void create(DataCenterRequestDto dcDto) {
        DataCenter dc = dataCenterMapper.toDataCenter(dcDto);
        dataCenterRepo.save(dc);
    }

    @Override
    @Transactional
    public void delete(int dataCenterId) {
        DataCenter dataCenter = dataCenterRepo.findById(dataCenterId).orElseThrow(() -> new DataCenterNotFoundException("Datacenter with id " + dataCenterId + " does not exist."));
        List<Server> servers = dataCenter.getServers();
        if(!servers.isEmpty())
        {
            servers.forEach(server -> server.setDatacenter(null));
            serverRepo.saveAll(servers);
        }
        dataCenterRepo.deleteById(dataCenterId);
    }

    @Override
    @Transactional
    public void update(DataCenterUpdateRequestDto dcDto, int dataCenterId) {
        DataCenter datacenter = dataCenterRepo.findById(dataCenterId).orElseThrow(() -> new DataCenterNotFoundException("Datacenter with id " + dataCenterId + " does not exist."));
        dataCenterMapper.updateDataCenterFromDto(dcDto, datacenter);
        dataCenterRepo.save(datacenter);
    }

    @Override
//    @Transactional(dontRollbackOn = Exception.class)
    @Transactional()
    public void addOrUpdateServers(DataCenterServersAddOrUpdateRequestDto serverIds, int dataCenterId) {
        DataCenter dc = dataCenterRepo.findById(dataCenterId).orElseThrow(() -> new DataCenterNotFoundException("Datacenter with id " + dataCenterId + " does not exist."));
        int newCurrentNoOfServers = dc.getCurrentNoOfServers() + serverIds.getServerIds().size();
        if (dc.getMaxNoOfServers() < newCurrentNoOfServers)
            throw new CurrentCapacityOverMaxCapacityException("Datacenter capacity for servers surpassed!");
        List<Server> servers = serverRepo.findAllById(serverIds.getServerIds());
        if (servers.size() != serverIds.getServerIds().size() || servers.isEmpty())
            throw new NotAllServersFoundException("Not all servers provided exist.");
        servers.forEach(s -> s.setDatacenter(dc));
        serverRepo.saveAll(servers);
        dc.setServers(servers);
        dc.setCurrentNoOfServers(servers.size());
        dataCenterRepo.save(dc);
    }

    @Override
    public List<DataCenterSummarizedDto> getAllSummarized() {
        return dataCenterRepo.findAllBy();
    }

}
