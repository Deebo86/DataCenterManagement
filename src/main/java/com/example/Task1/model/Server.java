package com.example.Task1.model;

import com.example.Task1.model.enums.OperatingSystem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Server {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "serverSeqGen")
    @SequenceGenerator(name="serverSeqGen", sequenceName = "server_sequence", allocationSize = 1)
    private int id;
    @ManyToOne
    private DataCenter datacenter;
    private Integer memory;
    private Integer noOfCPU;
    @Enumerated(EnumType.STRING)
    private OperatingSystem os;
}
