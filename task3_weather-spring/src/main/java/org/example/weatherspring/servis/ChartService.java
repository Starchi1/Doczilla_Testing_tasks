package org.example.weatherspring.servis;

import org.example.weatherspring.model.WeatherData;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.lines.SeriesLines;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ChartService {

    private static final Logger log = LoggerFactory.getLogger(ChartService.class);

    public byte[] generateTemperatureChart(WeatherData weatherData) throws IOException {
        try {
            log.info("Generating chart for city: {}", weatherData.getCity());

            if (weatherData == null) {
                throw new IllegalArgumentException("WeatherData is null");
            }

            List<WeatherData.HourlyTemperature> hourlyTemperatures = weatherData.getHourlyTemperatures();
            if (hourlyTemperatures == null || hourlyTemperatures.isEmpty()) {
                throw new IllegalArgumentException("No temperature data available for city: " + weatherData.getCity());
            }

            log.info("Found {} temperature data points", hourlyTemperatures.size());

            List<Date> times = new ArrayList<>();
            List<Double> temperatures = new ArrayList<>();

            for (WeatherData.HourlyTemperature hourly : hourlyTemperatures) {
                if (hourly.getTime() != null && hourly.getTemperature() != null) {
                    Date date = Date.from(hourly.getTime().atZone(ZoneId.systemDefault()).toInstant());
                    times.add(date);
                    temperatures.add(hourly.getTemperature());
                }
            }

            if (times.isEmpty() || temperatures.isEmpty()) {
                throw new IllegalArgumentException("No valid temperature data available for chart generation");
            }

            XYChart chart = new XYChartBuilder()
                    .width(800)
                    .height(600)
                    .title("Temperature Forecast for " + weatherData.getCity())
                    .xAxisTitle("Time")
                    .yAxisTitle("Temperature (°C)")
                    .build();

            chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
            chart.getStyler().setMarkerSize(8);
            chart.getStyler().setPlotBackgroundColor(Color.WHITE);
            chart.getStyler().setChartBackgroundColor(Color.WHITE);
            chart.getStyler().setAxisTickLabelsColor(Color.DARK_GRAY);
            chart.getStyler().setXAxisLabelRotation(45);
            chart.getStyler().setPlotGridLinesVisible(true);
            chart.getStyler().setPlotGridLinesColor(Color.LIGHT_GRAY);

            chart.getStyler().setDatePattern("HH:mm");

            XYSeries series = chart.addSeries("Temperature", times, temperatures);
            series.setLineStyle(SeriesLines.SOLID);
            series.setMarker(SeriesMarkers.CIRCLE);
            series.setLineColor(new Color(0, 100, 200));
            series.setMarkerColor(new Color(220, 50, 50));
            series.setLineWidth(3f);

            if (!temperatures.isEmpty()) {
                double minTemp = temperatures.stream()
                        .min(Double::compare)
                        .orElse(0.0) - 2;
                double maxTemp = temperatures.stream()
                        .max(Double::compare)
                        .orElse(30.0) + 2;
                chart.getStyler().setYAxisMin(minTemp);
                chart.getStyler().setYAxisMax(maxTemp);
            }

            byte[] chartBytes = BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG);
            log.info("Chart generated successfully, size: {} bytes", chartBytes.length);
            return chartBytes;

        } catch (Exception e) {
            log.error("Error generating chart for city: {}", weatherData != null ? weatherData.getCity() : "unknown", e);
            throw e;
        }
    }
}