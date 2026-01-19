package org.example.weatherspring.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonTypeName("WeatherData")
public class WeatherData implements Serializable {
    private static final long serialVersionUID = 2001L;

    private String city;
    private Double latitude;
    private Double longitude;
    private List<HourlyTemperature> hourlyTemperatures;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime cachedAt;

    public WeatherData() {
        this.cachedAt = LocalDateTime.now();
    }

    public WeatherData(String city, Double latitude, Double longitude,
                       List<HourlyTemperature> hourlyTemperatures) {
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.hourlyTemperatures = hourlyTemperatures;
        this.cachedAt = LocalDateTime.now();
    }

    public WeatherData(String city, Double latitude, Double longitude,
                       List<HourlyTemperature> hourlyTemperatures, LocalDateTime cachedAt) {
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.hourlyTemperatures = hourlyTemperatures;
        this.cachedAt = cachedAt != null ? cachedAt : LocalDateTime.now();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public List<HourlyTemperature> getHourlyTemperatures() {
        return hourlyTemperatures;
    }

    public void setHourlyTemperatures(List<HourlyTemperature> hourlyTemperatures) {
        this.hourlyTemperatures = hourlyTemperatures;
    }

    public LocalDateTime getCachedAt() {
        return cachedAt;
    }

    public void setCachedAt(LocalDateTime cachedAt) {
        this.cachedAt = cachedAt;
    }

    @Override
    public String toString() {
        return "WeatherData{" +
                "city='" + city + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", hourlyTemperatures=" + hourlyTemperatures +
                ", cachedAt=" + cachedAt +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    @JsonTypeName("HourlyTemperature")
    public static class HourlyTemperature implements Serializable {
        private static final long serialVersionUID = 2002L;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime time;
        private Double temperature;

        public HourlyTemperature() {
        }

        public HourlyTemperature(LocalDateTime time, Double temperature) {
            this.time = time;
            this.temperature = temperature;
        }

        public LocalDateTime getTime() {
            return time;
        }

        public void setTime(LocalDateTime time) {
            this.time = time;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        @Override
        public String toString() {
            return "HourlyTemperature{" +
                    "time=" + time +
                    ", temperature=" + temperature +
                    '}';
        }
    }
}