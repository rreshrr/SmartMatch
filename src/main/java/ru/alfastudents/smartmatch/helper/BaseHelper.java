package ru.alfastudents.smartmatch.helper;

import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;
import java.util.Scanner;

public abstract class BaseHelper {

    @Value("${app.comma-delimiter}")
    protected String COMMA_DELIMITER;

    protected Scanner getScannerFromFileCsv(String fileName) {
        InputStream inputStream = getClass().getResourceAsStream(fileName);

        if (inputStream == null) {
            throw new IllegalArgumentException("File not found: " + fileName);
        }

        return new Scanner(inputStream);
    }

}
