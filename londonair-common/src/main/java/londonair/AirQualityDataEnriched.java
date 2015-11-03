package londonair;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by jbrisbin on 10/30/15.
 */
public class AirQualityDataEnriched extends AirQualityData {

  @JsonProperty("TMP")
  private float temperature;
  @JsonProperty("WSPD")
  private float windSpeed;
  @JsonProperty("WDIR")
  private float windDirection;
  @JsonProperty("RHUM")
  private float relativeHumidity;

  public float getTemperature() {
    return temperature;
  }

  @Override
  public AirQualityDataEnriched setMeasurementDate(Date measurementDate) {
    super.setMeasurementDate(measurementDate);
    return this;
  }

  @Override
  public AirQualityDataEnriched setSite(Site site) {
    super.setSite(site);
    return this;
  }

  @Override
  public AirQualityDataEnriched setSpeciesCode(String speciesCode) {
    super.setSpeciesCode(speciesCode);
    return this;
  }

  @Override
  public AirQualityDataEnriched setValue(float value) {
    super.setValue(value);
    return this;
  }

  public AirQualityDataEnriched setTemperature(float temperature) {
    this.temperature = temperature;
    return this;
  }

  public float getWindSpeed() {
    return windSpeed;
  }

  public AirQualityDataEnriched setWindSpeed(float windSpeed) {
    this.windSpeed = windSpeed;
    return this;
  }

  public float getWindDirection() {
    return windDirection;
  }

  public AirQualityDataEnriched setWindDirection(float windDirection) {
    this.windDirection = windDirection;
    return this;
  }

  public float getRelativeHumidity() {
    return relativeHumidity;
  }

  public AirQualityDataEnriched setRelativeHumidity(float relativeHumidity) {
    this.relativeHumidity = relativeHumidity;
    return this;
  }

}
