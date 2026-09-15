package com.smartquote.controller;

import com.smartquote.service.PdfService;
import com.smartquote.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class PdfStreamController {

    private final PdfService pdfService;
    private final StorageService storageService;

    @Value("${storage.local.base-dir:./storage}")
    private String baseDir;

    public PdfStreamController(PdfService pdfService, StorageService storageService) {
        this.pdfService = pdfService;
        this.storageService = storageService;
    }

    @GetMapping(value = {
            "/files/{fileName:.+}",
            "/quotations/{fileName:.+}",
            "/files/quotations/{fileName:.+}"
    })
    public ResponseEntity<byte[]> getPdfFile(@PathVariable String fileName) {
        String cleanName = fileName.replace(".pdf", "");
        if (cleanName.contains("-")) {
            // handle timestamp prefix if present
            String[] parts = cleanName.split("-");
            if (parts.length >= 2) {
                cleanName = parts[parts.length - 2] + "-" + parts[parts.length - 1];
            }
        }

        Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
        Path directPath = basePath.resolve(fileName);
        Path subPath = basePath.resolve("quotations").resolve(fileName);

        byte[] pdfBytes = null;

        if (Files.exists(directPath)) {
            try {
                pdfBytes = Files.readAllBytes(directPath);
            } catch (IOException ignored) {}
        } else if (Files.exists(subPath)) {
            try {
                pdfBytes = Files.readAllBytes(subPath);
            } catch (IOException ignored) {}
        }

        if (pdfBytes == null) {
            try {
                pdfBytes = pdfService.generateDefaultPdf(cleanName);
                storageService.uploadFile(pdfBytes, fileName, "application/pdf");
            } catch (Exception e) {
                pdfBytes = pdfService.generateDefaultPdf("QT-319659");
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", fileName);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
