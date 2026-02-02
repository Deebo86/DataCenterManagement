package com.example.Task1.model;


import com.example.Task1.model.enums.DataCenterType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class DataCenter {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dataCenterSeqGen")
    @SequenceGenerator(name="dataCenterSeqGen", sequenceName = "data_center_sequence", allocationSize = 1)
    private int id;
    private String country;
    private String address;
    private int maxNoOfServers;
    private int currentNoOfServers;
    @OneToMany(mappedBy = "datacenter")
    private List<Server> servers;
    @Enumerated(EnumType.STRING)
    private DataCenterType dataCenterType;
}
