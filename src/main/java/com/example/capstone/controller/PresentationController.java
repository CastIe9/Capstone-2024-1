package com.example.demo.Controller;

import com.example.demo.Domain.Presentation;
import com.example.demo.Domain.PresentationFile;
import com.example.demo.Service.PresentationService;

import com.example.demo.dto.PresentationRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/presentations")
@RequiredArgsConstructor
public class PresentationController {
    private static final Logger logger = LoggerFactory.getLogger(PresentationController.class);
    private final PresentationService presentationService;

    @PostMapping("/create")
    public ResponseEntity<?> createAndDownloadPresentation(
            @PathVariable Long porfolioID,
            @RequestBody PresentationRequest presentationRequest, HttpServletRequest request) {
        try {
            logger.info("PowerPoint presentation creating started.");
            Presentation presentation = presentationService.createShortPPT(presentationRequest);
            byte[] pptData = presentationService.createPowerPoint(presentation);
            Long userId = (Long) request.getAttribute("userId");
            presentationService.savePresentationFile(pptData, userId, porfolioID);

            logger.info("PowerPoint presentation created and saved successfully.");
            return ResponseEntity.ok("Presentation saved successfully");
        } catch (RuntimeException e) {
            logger.error("Error occurred while creating presentation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error occurred while creating presentation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPresentationFilesByUserId(@PathVariable Long userId) {
        try {
            List<PresentationFile> presentationFiles = presentationService.getPresentationFilesByUserId(userId);
            if (presentationFiles.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No presentations found for this user ID");
            }
            return ResponseEntity.ok(presentationFiles);
        } catch (Exception e) {
            logger.error("Error occurred while fetching presentations for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<?> getPresentationsByPortfolioId(@PathVariable Long portfolioId) {
        try {
            List<PresentationFile> presentations = presentationService.getPresentationsByPortfolioId(portfolioId);
            if (presentations.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No presentations found for this portfolio ID");
            }
            return ResponseEntity.ok(presentations);
        } catch (Exception e) {
            logger.error("Error occurred while fetching presentations for portfolio ID {}: {}", portfolioId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error occurred: " + e.getMessage());
        }
    }
}
