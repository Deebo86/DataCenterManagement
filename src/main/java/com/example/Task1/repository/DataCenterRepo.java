package com.example.Task1.repository;

import com.example.Task1.dto.DataCenterSummarizedDto;
import com.example.Task1.model.DataCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataCenterRepo extends JpaRepository<DataCenter, Integer> {

//    @Query("select dc from DataCenter dc join Server s on s.datacenter = dc") //this inner joins; returns only the datacenters with servers
    @Query("select dc from DataCenter dc") //this does a left/right join implicitly where it retrieves all of datacenter and if there is a server to that datacenter, it gets it too
    List<DataCenter> getAll();

    @NativeQuery(value = "select dc.* from data_center dc left join server s on dc.id = s.datacenter_id where dc.id = :dataCenterId")
    Optional<DataCenter> findById(int dataCenterId);

    List<DataCenterSummarizedDto> findAllBy();
}
