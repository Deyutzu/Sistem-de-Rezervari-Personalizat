package com.stayfinder.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherCodeMapperTest {

    @Test
    @DisplayName("Cod 0 se mapeaza la Senin si iconita sun")
    void mapsClearSky() {
        WeatherCodeMapper.WeatherInfo info = WeatherCodeMapper.map(0);
        assertThat(info.description()).isEqualTo("Senin");
        assertThat(info.icon()).isEqualTo("sun");
    }

    @Test
    @DisplayName("Coduri de ploaie se mapeaza la Ploaie si iconita rain")
    void mapsRain() {
        WeatherCodeMapper.WeatherInfo info = WeatherCodeMapper.map(61);
        assertThat(info.description()).isEqualTo("Ploaie");
        assertThat(info.icon()).isEqualTo("rain");
    }

    @Test
    @DisplayName("Coduri de ninsoare se mapeaza la Ninsoare si iconita snow")
    void mapsSnow() {
        WeatherCodeMapper.WeatherInfo info = WeatherCodeMapper.map(71);
        assertThat(info.description()).isEqualTo("Ninsoare");
        assertThat(info.icon()).isEqualTo("snow");
    }

    @Test
    @DisplayName("Coduri de furtuna se mapeaza la Furtuna si iconita thunder")
    void mapsThunder() {
        WeatherCodeMapper.WeatherInfo info = WeatherCodeMapper.map(95);
        assertThat(info.description()).isEqualTo("Furtuna");
        assertThat(info.icon()).isEqualTo("thunder");
    }

    @Test
    @DisplayName("Cod necunoscut se mapeaza fallback la Variabil si iconita clouds")
    void mapsDefaultFallback() {
        WeatherCodeMapper.WeatherInfo info = WeatherCodeMapper.map(999);
        assertThat(info.description()).isEqualTo("Variabil");
        assertThat(info.icon()).isEqualTo("clouds");
    }
}
