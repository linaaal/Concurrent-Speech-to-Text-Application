package comp3011.assignment1.controller;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import comp3011.assignment1.dto.ErrorResponse;
import comp3011.assignment1.dto.TranscriptionResponse;
import comp3011.assignment1.service.OpenAiTranscriptionService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/transcriptions")
public class TranscriptionController {

    private final OpenAiTranscriptionService transcriptionService;

    public TranscriptionController(
            OpenAiTranscriptionService transcriptionService) {

        this.transcriptionService = transcriptionService;
    }

	// Receives an audio recording and returns its transcription.
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> createTranscription(
            @RequestPart("audio") MultipartFile audioFile,
            HttpServletRequest request) {

        // Reject an empty recording before contacting OpenAI
        if (audioFile.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            Instant.now(),
                            HttpStatus.BAD_REQUEST.value(),
                            HttpStatus.BAD_REQUEST.getReasonPhrase(),
                            "An audio file must be provided.",
                            request.getRequestURI()));
        }

        String text =
                transcriptionService.transcribe(audioFile);

        return ResponseEntity.ok(
                new TranscriptionResponse(text));
    }
}