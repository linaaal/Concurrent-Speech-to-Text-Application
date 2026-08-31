package comp3011.assignment1.config;


// implement YAML endpoint -  /api/v1/admin/uptime
import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


// tells Spring that this class contains application configuration 
@Configuration
public class TimeConfiguration {

	// creates a UTC Clock object that Spring can provide to other classes
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}