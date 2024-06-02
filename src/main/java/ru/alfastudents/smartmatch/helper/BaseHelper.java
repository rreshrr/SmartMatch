package ru.alfastudents.smartmatch.helper;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;

import java.util.Scanner;

public abstract class BaseHelper {

    @Setter
    protected String sourceFilePath = null;

    protected abstract String getSourceFilePath();

    @Value("${app.comma-delimiter}")
    protected String COMMA_DELIMITER;

    protected Scanner getScannerFromFileCsv(String filePath) {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new FileNotFoundException("Файл не найден: " + filePath);
            }
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            return new Scanner(bis);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Ошибка при открытии файла: " + filePath, e);
        }
    }
}
