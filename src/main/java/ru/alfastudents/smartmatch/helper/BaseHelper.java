package ru.alfastudents.smartmatch.helper;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Scanner;

public abstract class BaseHelper {

    @Setter
    @Getter
    protected String resourceFilePath = null;

    @Value("${app.comma-delimiter}")
    protected String COMMA_DELIMITER;

    protected Scanner getScannerFromFileCsv(String absoluteFilePath) {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            File file = new File(absoluteFilePath);
            if (!file.exists()) {
                throw new FileNotFoundException("Файл не найден: " + absoluteFilePath);
            }
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            return new Scanner(bis);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Ошибка при открытии файла: " + absoluteFilePath, e);
        }
    }

    protected Scanner getScannerFromResourceFileCsv(String resourceFilePath) {
        try {InputStream inputStream = getClass().getResourceAsStream(resourceFilePath);
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + resourceFilePath);
            }
            return new Scanner(inputStream);
        } catch (Exception e){
            throw new RuntimeException("Ошибка при открытии файла: " + resourceFilePath, e);
        }
    }

    protected abstract Scanner getScannerWithCsvContent();
}
