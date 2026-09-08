package comp3011.assignment1.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import comp3011.assignment1.service.OpenAiTranscriptionService;

@RestController
@RequestMapping("/api/v1/transcriptions")
public class TranscriptionController {

    private final OpenAiTranscriptionService transcriptionService;

    public TranscriptionController(
            OpenAiTranscriptionService transcriptionService) {

        this.transcriptionService = transcriptionService;
    }
}