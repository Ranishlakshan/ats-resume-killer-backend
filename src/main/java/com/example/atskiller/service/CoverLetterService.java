package com.example.atskiller.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.Loader;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;

@Service
public class CoverLetterService {

    private final CoverLetterGenerationService coverLetterGenerationService;

    @Autowired
    public CoverLetterService(CoverLetterGenerationService coverLetterGenerationService) {
        this.coverLetterGenerationService = coverLetterGenerationService;
    }

    public Mono<String> generateCoverLetter(MultipartFile resumeFile, String jobDescription) {
        try {
            String resumeText = extractPdfText(resumeFile);

            return coverLetterGenerationService.generateCoverLetterFromOpenAI(jobDescription, resumeText)
                    .map(jsonObject -> jsonObject.optString("coverLetter", "Error: Could not generate cover letter."));

        } catch (IOException e) {
            return Mono.error(new RuntimeException("Error processing resume PDF: " + e.getMessage(), e));
        }
    }

    private String extractPdfText(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            return new PDFTextStripper().getText(document);
        }
    }
}
