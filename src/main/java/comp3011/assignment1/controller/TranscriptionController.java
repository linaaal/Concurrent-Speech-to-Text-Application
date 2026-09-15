package comp3011.assignment1.controller;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import comp3011.assignment1.dto.ErrorResponse;
import comp3011.assignment1.dto.TranscriptionResponse;
import comp3011.assignment1.service.OpenAiTranscriptionService;
import jakarta.servlet.http.HttpServletRequest;

// Marks this class as a REST controller for transcription requests
@RestController
@RequestMapping("/api/v1/transcriptions")
public class TranscriptionController {

    private final OpenAiTranscriptionService transcriptionService;

    // Receives the transcription service through dependency injection
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

        try {
            // Sends the uploaded audio file to the transcription service
            String text =
                    transcriptionService.transcribe(audioFile);

            // Returns the completed transcription with status 200
            return ResponseEntity.ok(
                    new TranscriptionResponse(text));

        } catch (RestClientResponseException exception) {
            // Return a safe error without exposing details from the external OpenAI response.

            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(new ErrorResponse(
                            Instant.now(),
                            HttpStatus.BAD_GATEWAY.value(),
                            HttpStatus.BAD_GATEWAY.getReasonPhrase(),
                            "The transcription service could not process the audio.",
                            request.getRequestURI()));

        } catch (RuntimeException exception) {
            // Handles any other unexpected error inside the application
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            Instant.now(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                            "Unable to create the transcription.",
                            request.getRequestURI()));
        }
    }
}