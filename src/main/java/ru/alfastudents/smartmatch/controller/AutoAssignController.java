package ru.alfastudents.smartmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.alfastudents.smartmatch.processor.AutoAssignProcessor;
import ru.alfastudents.smartmatch.entity.AutoAssignCase;
import ru.alfastudents.smartmatch.repository.AutoAssignCaseRepository;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class AutoAssignController {

    @Autowired
    private final AutoAssignProcessor autoAssignProcessor;

    @Autowired
    private final AutoAssignCaseRepository autoAssignCaseRepository;

    @GetMapping("autoassign/start")
    public String startAutoAssignProcess(){
        autoAssignProcessor.process();
        return "AutoAssignProcess finished.";
    }

    @GetMapping("autoassign/clear")
    public String clearAutoAssignCases(){
        autoAssignCaseRepository.deleteAll();
        return "Sucessfully cleared autoassign cases.";
    }

    @GetMapping("autoassign")
    public List<AutoAssignCase> getAutoAssignCases(){
        return autoAssignCaseRepository.findAll();
    }

}
