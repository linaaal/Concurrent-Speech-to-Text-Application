# COMP3011 Assignment 1 – Speech to Text

## Project Overview

This project is a Spring Boot speech-to-text application. It allows a user to record audio in their browser and sends the recording to a Java REST API. The Java backend then sends the audio to the OpenAI transcription service and returns the transcription to the webpage.

## Features

* Record audio using the browser microphone
* Stop and submit a recording
* Display the returned transcription
* Show clear recording, loading and error states
* Report the server uptime
* Report cumulative input and output token usage
* Shut down the application gracefully
* Handle more than 200 concurrent blocking HTTP requests
* Display a responsive interface using Bootstrap

## Technologies

* Java 25
* Spring Boot 4.1.0
* Maven
* HTML, CSS and JavaScript
* Bootstrap 5.3
* OpenAI Audio Transcription API
* JUnit 5 and Mockito

## Configuration

The application reads its OpenAI API key dynamically from the `OPENAI_API_KEY` operating-system environment variable.

The real key is not stored in the Java code or `application.properties`. It must not be printed, logged or committed to the repository.

For local testing on macOS or Linux:

```bash
export OPENAI_API_KEY="your-api-key"
```

The transcription model can optionally be changed using:

```bash
export OPENAI_TRANSCRIPTION_MODEL="gpt-4o-mini-transcribe"
```

The OpenAI base URL can also be changed for testing:

```bash
export OPENAI_BASE_URL="https://api.openai.com/v1"
```

TITAN provides `OPENAI_API_KEY` automatically when the packaged application is tested.

## Running the Application

### Spring Tools for Eclipse

1. Import the project as an existing Maven project.
2. Open `Assignment1Application.java`.
3. Select **Run As → Spring Boot App**.
4. Open:

```text
http://localhost:8080
```

### Packaged JAR

Build the application with:

```bash
mvn clean package
```

Run the generated JAR with:

```bash
java -jar target/Assignment1-0.0.1-SNAPSHOT.jar
```
