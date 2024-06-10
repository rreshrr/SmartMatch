package ru.alfastudents.smartmatch.integration.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(id, client.id) && Objects.equals(grade, client.grade) && Objects.equals(type, client.type) && Objects.equals(region, client.region) && Objects.equals(name, client.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, grade, type, region, name);
    }
}
