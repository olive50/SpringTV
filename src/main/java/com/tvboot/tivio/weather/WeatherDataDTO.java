package com.tvboot.tivio.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherDataDTO {

    @JsonProperty("name")
    private String cityName;

    @JsonProperty("cod")
    private Integer statusCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("main")
    private MainDTO main;

    @JsonProperty("weather")
    private List<WeatherDTO> weather;

    @JsonProperty("wind")
    private WindDTO wind;

    @JsonProperty("clouds")
    private CloudsDTO clouds;

    @JsonProperty("sys")
    private SystemDTO sys;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MainDTO {
        @JsonProperty("temp")
        private Double temperature;

        @JsonProperty("feels_like")
        private Double feelsLike;

        @JsonProperty("temp_min")
        private Double temperatureMin;

        @JsonProperty("temp_max")
        private Double temperatureMax;

        @JsonProperty("humidity")
        private Integer humidity;

        @JsonProperty("pressure")
        private Integer pressure;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WeatherDTO {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("main")
        private String main;

        @JsonProperty("description")
        private String description;

        @JsonProperty("icon")
        private String icon;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WindDTO {
        @JsonProperty("speed")
        private Double speed;

        @JsonProperty("deg")
        private Integer degree;

        @JsonProperty("gust")
        private Double gust;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CloudsDTO {
        @JsonProperty("all")
        private Integer cloudiness;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SystemDTO {
        @JsonProperty("country")
        private String country;

        @JsonProperty("sunrise")
        private Long sunrise;

        @JsonProperty("sunset")
        private Long sunset;
    }
}