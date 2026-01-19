package org.example.weatherspring.servis;

import org.example.weatherspring.model.GeocodingResponse;
import org.example.weatherspring.model.WeatherData;
import org.example.weatherspring.model.WeatherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class WeatherService {

    private final WeatherApiService weatherApiService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    public WeatherService(WeatherApiService weatherApiService) {
        this.weatherApiService = weatherApiService;
    }

    @Cacheable(value = "weatherData", key = "#city.toLowerCase()")
    public WeatherData getWeatherData(String city) {
        log.info("Fetching fresh weather data for city: {}", city);

        // Получаем координаты города
        GeocodingResponse.GeocodingResult location = weatherApiService.getCoordinates(city);

        // Получаем прогноз погоды
        WeatherResponse weatherResponse = weatherApiService
                .getWeatherForecast(location.getLatitude(), location.getLongitude());

        // Проверяем данные
        if (weatherResponse.getHourly() == null ||
                weatherResponse.getHourly().getTime() == null ||
                weatherResponse.getHourly().getTemperature2m() == null) {
            throw new RuntimeException("Invalid weather data received for city: " + city);
        }


        int dataPoints = Math.min(24, Math.min(
                weatherResponse.getHourly().getTime().size(),
                weatherResponse.getHourly().getTemperature2m().size()
        ));

        List<WeatherData.HourlyTemperature> hourlyTemperatures = IntStream.range(0, dataPoints)
                .mapToObj(i -> new WeatherData.HourlyTemperature(
                        LocalDateTime.parse(weatherResponse.getHourly().getTime().get(i), formatter),
                        weatherResponse.getHourly().getTemperature2m().get(i)
                ))
                .collect(Collectors.toList());

        return new WeatherData(
                city,
                location.getLatitude(),
                location.getLongitude(),
                hourlyTemperatures,
                LocalDateTime.now()
        );
    }
}