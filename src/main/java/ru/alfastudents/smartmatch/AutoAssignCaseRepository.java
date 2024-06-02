package ru.alfastudents.smartmatch;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface AutoAssignCaseRepository extends CrudRepository<AutoAssignCase, UUID> {
}
