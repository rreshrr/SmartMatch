package ru.alfastudents.smartmatch.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Entity
@Table(name="autoassignes", schema = "smartmatch")
public class AutoAssignCase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "client_id", nullable = false, updatable = false)
    private String clientId;

    @Column(name = "manager_id", nullable = false, updatable = false)
    private String managerId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AutoAssignCase(String clientId, String managerId) {
        this.clientId = clientId;
        this.managerId = managerId;
    }
}
