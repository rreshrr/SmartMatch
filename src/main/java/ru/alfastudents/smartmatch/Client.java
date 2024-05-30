package ru.alfastudents.smartmatch;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class Client {

    private UUID id;

    private String grade;

    private String type;

    private String region;

    public boolean isDigital(){
        return type.equals("Digital");
    }

    public boolean isOrdinary(){
        return grade.equals("Ordinary");
    }
}
