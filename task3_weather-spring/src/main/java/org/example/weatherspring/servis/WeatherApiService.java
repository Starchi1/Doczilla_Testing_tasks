package org.example.weatherspring.servis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.weatherspring.model.GeocodingResponse;
import org.example.weatherspring.model.WeatherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class WeatherApiService {

    private final RestTemplate restTemplate;
    private static final Logger log = LoggerFactory.getLogger(WeatherApiService.class);
    private static final String GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private static final String WEATHER_URL = "https://api.open-meteo.com/v1/forecast";

    public WeatherApiService(ObjectMapper apiObjectMapper) {
        this.restTemplate = new RestTemplate();

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(apiObjectMapper);

        this.restTemplate.getMessageConverters().removeIf(
                mc -> mc instanceof MappingJackson2HttpMessageConverter
        );
        this.restTemplate.getMessageConverters().add(converter);
    }

    public GeocodingResponse.GeocodingResult getCoordinates(String city) {
        log.info("Fetching coordinates for city: {}", city);

        String url = UriComponentsBuilder.fromHttpUrl(GEOCODING_URL)
                .queryParam("name", city)
                .queryParam("count", 1)
                .queryParam("language", "en")
                .queryParam("format", "json")
                .toUriString();

        GeocodingResponse response = restTemplate.getForObject(url, GeocodingResponse.class);

        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            throw new RuntimeException("City not found: " + city);
        }

        return response.getResults().get(0);
    }

    public WeatherResponse getWeatherForecast(Double latitude, Double longitude) {
        log.info("Fetching weather for coordinates: {}, {}", latitude, longitude);

        String url = UriComponentsBuilder.fromHttpUrl(WEATHER_URL)
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("hourly", "temperature_2m")
                .queryParam("forecast_days", 1)
                .toUriString();

        WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);

        if (response == null || response.getHourly() == null) {
            throw new RuntimeException("Weather data not available for coordinates: " +
                    latitude + ", " + longitude);
        }

        return response;
    }
}