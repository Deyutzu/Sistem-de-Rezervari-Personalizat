package com.stayfinder.external;

/**
 * Mapează codurile meteo WMO (World Meteorological Organization) folosite de Open-Meteo
 * într-o descriere în limba română și într-un identificator de iconiță simplu
 * ("sun", "clouds", "rain", "snow", "thunder").
 */
public final class WeatherCodeMapper {

    private WeatherCodeMapper() {
    }

    public record WeatherInfo(String description, String icon) {}

    public static WeatherInfo map(int weatherCode) {
        return switch (weatherCode) {
            case 0 -> new WeatherInfo("Senin", "sun");
            case 1, 2 -> new WeatherInfo("Partial innorat", "clouds");
            case 3 -> new WeatherInfo("Innorat", "clouds");
            case 45, 48 -> new WeatherInfo("Ceata", "clouds");
            case 51, 53, 55 -> new WeatherInfo("Burnita", "rain");
            case 56, 57, 66, 67 -> new WeatherInfo("Ploaie inghetata", "snow");
            case 61, 63, 65 -> new WeatherInfo("Ploaie", "rain");
            case 71, 73, 75, 77 -> new WeatherInfo("Ninsoare", "snow");
            case 80, 81, 82 -> new WeatherInfo("Averse de ploaie", "rain");
            case 85, 86 -> new WeatherInfo("Averse de ninsoare", "snow");
            case 95, 96, 99 -> new WeatherInfo("Furtuna", "thunder");
            default -> new WeatherInfo("Variabil", "clouds");
        };
    }
}
