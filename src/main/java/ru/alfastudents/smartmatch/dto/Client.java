package ru.alfastudents.smartmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Client {

    private String id;

    private String grade;

    private String type;

    private String region;

    public boolean isDigital(){
        return type.equalsIgnoreCase("Digital");
    }

    public boolean isOrdinary(){
        return grade.equalsIgnoreCase("Ordinary");
    }

    public Boolean isMajor(){
        return grade.equalsIgnoreCase("Major");
    }
}
