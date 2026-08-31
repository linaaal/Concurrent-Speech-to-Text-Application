package comp3011.assignment1;

import java.time.Instant

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Assignment1Application {

	public static void main(String[] args) {
		SpringApplication.run(Assignment1Application.class, args);
	}
	
	// represents the successful shutdown response from the yaml
	public record ShutdownResponse(String message) {
		
	}
	
	public record ErrorResponse(
			Instant timestamp,
			int status,
			String error,
			String message,
			String path) {
	
	}
	



}
