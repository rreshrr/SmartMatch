package ru.alfastudents.smartmatch.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Entity
@Getter
@Table(name="autoassignecases", schema = "smartmatch")
public class AutoAssignCase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "id_client", nullable = false, updatable = false)
    private String clientId;

    @Column(name = "id_manager", nullable = false, updatable = false)
    private String managerId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AutoAssignCase(String clientId, String managerId) {
        this.clientId = clientId;
        this.managerId = managerId;
    }
}
