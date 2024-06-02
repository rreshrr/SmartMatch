package ru.alfastudents.smartmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.alfastudents.smartmatch.AutoAssignProcessor;

@RequiredArgsConstructor
@RestController
public class AutoAssignController {

    @Autowired
    private final AutoAssignProcessor autoAssignProcessor;

    @GetMapping("autoassign/start")
    public String startAutoAssignProcess(){
        autoAssignProcessor.process();
        return "AutoAssignProcess finished.";
    }

}
