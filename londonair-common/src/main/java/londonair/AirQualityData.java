package londonair;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * Created by jbrisbin on 10/30/15.
 */
public class AirQualityData {

  private final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

  private Date   measurementDate;
  private Site   site;
  private String speciesCode;
  private float value = -1;

  @JsonIgnore
  public Date getMeasurementDate() {
    return measurementDate;
  }

  @JsonGetter("@MeasurementDateGMT")
  public String getMeasurementDateAsString() {
    return fmt.print(measurementDate.getTime());
  }

  @JsonSetter("@MeasurementDateGMT")
  public AirQualityData setMeasurementDate(String measurementDate) {
    return setMeasurementDate(fmt.parseDateTime(measurementDate).toDate());
  }

  public AirQualityData setMeasurementDate(Date measurementDate) {
    this.measurementDate = measurementDate;
    return this;
  }

  @JsonGetter("@Site")
  public Site getSite() {
    return site;
  }

  @JsonSetter("@Site")
  public AirQualityData setSite(Site site) {
    this.site = site;
    return this;
  }

  @JsonGetter("@SpeciesCode")
  public String getSpeciesCode() {
    return speciesCode;
  }

  @JsonSetter("@SpeciesCode")
  public AirQualityData setSpeciesCode(String speciesCode) {
    this.speciesCode = speciesCode;
    return this;
  }

  @JsonGetter("@Value")
  public String getValueAsString() {
    return value > 0 ? String.valueOf(value) : "";
  }

  @JsonIgnore
  public float getValue() {
    return value;
  }

  @JsonSetter("@Value")
  public AirQualityData setValue(float value) {
    this.value = value;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AirQualityData that = (AirQualityData) o;

    if (!measurementDate.equals(that.measurementDate)) return false;
    if (!site.equals(that.site)) return false;
    return speciesCode.equals(that.speciesCode);

  }

  @Override
  public int hashCode() {
    int result = measurementDate.hashCode();
    result = 31 * result + site.hashCode();
    result = 31 * result + speciesCode.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "AirQualityData{" +
           "measurementDate=" + measurementDate +
           ", site='" + site + '\'' +
           ", speciesCode='" + speciesCode + '\'' +
           ", value=" + value +
           '}';
  }

}
