package org.example.weatherspring.controller;

import org.example.weatherspring.model.WeatherData;
import org.example.weatherspring.servis.ChartService;
import org.example.weatherspring.servis.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;
    private final ChartService chartService;
    private static final Logger log = LoggerFactory.getLogger(WeatherController.class);

    public WeatherController(ChartService chartService, WeatherService weatherService) {
        this.chartService = chartService;
        this.weatherService = weatherService;
    }

    @GetMapping
    public ResponseEntity<?> getWeather(@RequestParam String city) {
        try {
            log.info("Weather request for city: {}", city);
            WeatherData weatherData = weatherService.getWeatherData(city);
            return ResponseEntity.ok(weatherData);
        } catch (Exception e) {
            log.error("Error fetching weather data for city: {}", city, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error fetching weather data: " + e.getMessage());
        }
    }

    @GetMapping("/chart")
    public ResponseEntity<byte[]> getWeatherChart(@RequestParam String city) {
        try {
            log.info("Chart request for city: {}", city);

            WeatherData weatherData = weatherService.getWeatherData(city);

            byte[] chartImage = chartService.generateTemperatureChart(weatherData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(chartImage.length);
            headers.setCacheControl("max-age=300");

            log.info("Chart successfully generated for city: {}, size: {} bytes",
                    city, chartImage.length);

            return new ResponseEntity<>(chartImage, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.error("Invalid data for chart generation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error generating chart for city: {}", city, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}