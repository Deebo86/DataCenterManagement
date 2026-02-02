package com.example.Task1.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userSeqGen")
    @SequenceGenerator(name="userSeqGen", sequenceName = "user_sequence", allocationSize = 1)
    private int id;
    @Column(unique = true)
    private String username;
    private String password;
    @ElementCollection(fetch = FetchType.EAGER, targetClass = String.class)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

}
