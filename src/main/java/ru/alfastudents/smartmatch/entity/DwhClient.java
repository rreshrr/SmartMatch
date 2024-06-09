package ru.alfastudents.smartmatch.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import jakarta.persistence.GenerationType;
import java.util.UUID;

@Entity
@Table(name = "dwh_clients", schema = "smartmatch")
public class DwhClient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "grade", nullable = false, updatable = true)
    private String clientGrade;

    @Column(name = "type", nullable = false, updatable = true)
    private String clientType;

    @Column(name = "region_fias", nullable = false, updatable = true)
    private String region;

}
