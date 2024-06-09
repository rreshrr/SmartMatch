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
import ru.alfastudents.smartmatch.integration.model.Client;
import ru.alfastudents.smartmatch.integration.model.Manager;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Entity
@Getter
@Table(name="autoassign_cases", schema = "smartmatch")
public class AutoAssignCase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "id_client", nullable = false, updatable = false)
    private String clientId;

    @Column(name = "id_manager", nullable = true, updatable = false)
    private String managerId;

    @Column(name = "client_region", nullable = true, updatable = false)
    private String clientRegion;

    @Column(name = "client_name", nullable = true, updatable = false)
    private String clientName;

    @Column(name = "client_type", nullable = true, updatable = false)
    private String clentType;

    @Column(name = "manager_name", nullable = true, updatable = false)
    private String managerName;

    @Column(name = "error", nullable = true, updatable = false)
    private String errorInfo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AutoAssignCase(Client client, Manager manager, String errorInfo) {
        this(client, manager);
        this.errorInfo = errorInfo;
    }

    public AutoAssignCase(Client client, Manager manager) {
        if (client != null){
            clientId = client.getId();
            clentType = client.getType();
            clientName = client.getName();
            clientRegion = client.getRegion();
        }
        if (manager != null){
            managerId = manager.getId();
            managerName = manager.getName();
        }
    }
}
