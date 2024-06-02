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
        return type.equals("Digital");
    }

    public boolean isOrdinary(){
        return grade.equals("Ordinary");
    }

    public Boolean isMajor(){
        return grade.equals("Major");
    }
}
