package ru.alfastudents.smartmatch.integration.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Client {

    private final String id;

    private final String grade;

    private final String type;

    private final String region;

    private final String name;

    public boolean isDigital(){
        return type.equalsIgnoreCase("digital");
    }

    public boolean isOrdinary(){
        return grade.equalsIgnoreCase("ordinary");
    }

    public Boolean isMajor(){
        return grade.equalsIgnoreCase("major");
    }
}
