# COMP3011 Assignment 1 – Speech to Text

## Project Overview

This project is a Spring Boot speech-to-text application. It allows a user to record audio in their browser and sends the recording to a Java REST API. The Java backend then sends the audio to the OpenAI transcription service and returns the transcription to the webpage.
## Features

* Record audio using the browser microphone
* Stop and submit a recording for transcription
* Display the returned transcription
* Show recording, loading, completion and error states
* Report server uptime
* Report cumulative input and output token usage
* Shut down the application gracefully
* Handle more than 200 simultaneous blocking HTTP requests
* Provide a responsive interface using Bootstrap

## High-Level Design

The browser records audio using the `MediaRecorder` API. JavaScript sends the recording to the Spring Boot transcription endpoint as multipart form data.

The transcription controller validates the request and passes the audio to the transcription service. The service communicates with the external OpenAI transcription API and records the returned token usage.

Separate controllers and services manage server uptime, global token statistics and graceful shutdown. Spring dependency injection is used to connect these components.

## Key Design Choices

* Spring Boot virtual threads support more than 200 simultaneous blocking HTTP requests.
* Atomic values protect shared statistics and shutdown state from race conditions.
* The OpenAI API key is loaded from an environment variable instead of being stored in the repository.
* External API details are configurable through application properties.
* Request logging records safe operational information without logging API keys, audio data or transcription text.
* The browser records audio at a reduced bitrate to decrease upload size and network usage.

## Technologies

* Java 25
* Spring Boot 4.1.0
* Maven
* HTML, CSS and JavaScript
* Bootstrap 5.3
* OpenAI Audio Transcription API
* JUnit 5 and Mockito

## Configuration

The application reads the OpenAI API key from the `OPENAI_API_KEY` operating-system environment variable.

The real API key is not stored in the Java source code or `application.properties`. It must not be printed, logged or committed to the repository.

For local testing on macOS or Linux:

```bash
export OPENAI_API_KEY="your-api-key"
```

The transcription model can optionally be changed with:

```bash
export OPENAI_TRANSCRIPTION_MODEL="gpt-4o-mini-transcribe"
```

The OpenAI base URL can also be changed. This supports local testing with a stub service:

```bash
export OPENAI_BASE_URL="https://api.openai.com/v1"
```

TITAN provides `OPENAI_API_KEY` automatically when the packaged application is tested.

## Running the Application

Java 25 must be selected when building the project.

### Spring Tools for Eclipse

1. Import the project as an existing Maven project.
2. Confirm that the project is configured to use Java 25.
3. Open `Assignment1Application.java`.
4. Select **Run As → Spring Boot App**.
5. Open `http://localhost:8080` in a browser.

### Packaged JAR

Build and run the tests with:

```bash
mvn clean package
```

Run the generated JAR with:

```bash
java -jar target/Assignment1-0.0.1-SNAPSHOT.jar
```

Then open:

```text
http://localhost:8080
```

## REST API Overview

| Method | Endpoint                 | Purpose                     |
| ------ | ------------------------ | --------------------------- |
| `POST` | `/api/v1/transcriptions` | Submit an audio recording   |
| `GET`  | `/api/v1/admin/uptime`   | View server uptime          |
| `POST` | `/api/v1/admin/shutdown` | Request graceful shutdown   |
| `GET`  | `/api/v1/global/stats`   | View cumulative token usage |

## Regression Testing

The project contains regression tests for its REST controllers, services, logging, concurrency and shared state.

External transcription requests are replaced with stubbed or mocked responses during testing. This keeps the tests repeatable, prevents charges and ensures that a real API key is not required.

The regression suite includes:

* successful and unsuccessful transcription requests;
* controller response and error handling;
* OpenAI response handling;
* safe request logging;
* global statistics updates;
* race-condition detection;
* graceful shutdown behaviour;
* 220 simultaneous blocking HTTP requests.

The purpose, expected results and assurances provided by specialised tests are documented beside their relevant test classes.

The complete Maven test run currently contains 18 tests. All 18 tests pass without failures, errors or skipped tests.
