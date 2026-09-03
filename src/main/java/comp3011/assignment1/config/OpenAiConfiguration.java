package comp3011.assignment1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenAiConfiguration {

    /*
     * Creates one reusable HTTP client for communicating with OpenAI.
     * Spring can provide this client to the transcription service later.
     */
    @Bean
    RestClient openAiRestClient(
            RestClient.Builder builder,
            @Value("${openai.base-url}") String baseUrl,
            @Value("${openai.api-key}") String apiKey) {

        // Configure the shared client with the selected OpenAI address
        RestClient.Builder clientBuilder =
                builder.baseUrl(baseUrl);

        /*
         * Add the secret bearer token when it is available.
         * The key is never printed or written into the source code.
         */
        if (!apiKey.isBlank()) {
            clientBuilder.defaultHeader(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + apiKey);
        }

        return clientBuilder.build();
    }
}