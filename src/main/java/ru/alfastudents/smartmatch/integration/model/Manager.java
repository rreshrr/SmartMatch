package ru.alfastudents.smartmatch.integration.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
public class Manager {

    private final String id;

    private final String name;

    private final String type;

    private final String region;

    private final Boolean isHead;

    @Setter
    private Boolean isSick;

    @Setter
    private Boolean isVacation;

    private final Integer clientCount;

    private final String grade;

    private final String email;

    public Boolean isOrdinary(){
        return grade.equalsIgnoreCase("ordinary");
    }

    public Boolean isMajor(){
        return grade.equalsIgnoreCase("major");
    }
}
