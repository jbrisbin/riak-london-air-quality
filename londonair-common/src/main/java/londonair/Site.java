package londonair;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * Created by jbrisbin on 11/2/15.
 */
public class Site {

  private final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

  private String code;
  private String name;
  private Date   opened;
  private Date   closed;
  private float latitude;
  private float longitude;

  @JsonGetter("@SiteCode")
  public String getCode() {
    return code;
  }

  @JsonSetter("@SiteCode")
  public Site setCode(String code) {
    this.code = code;
    return this;
  }

  @JsonGetter("@SiteName")
  public String getName() {
    return name;
  }

  @JsonSetter("@SiteName")
  public Site setName(String name) {
    this.name = name;
    return this;
  }

  @JsonGetter("@DateOpened")
  public String getOpenedAsString() {
    if (null == opened) {
      return "";
    }
    return fmt.print(opened.getTime());
  }

  @JsonIgnore
  public Date getOpened() {
    return opened;
  }

  @JsonSetter("@DateOpened")
  public Site setOpened(String opened) {
    if (StringUtils.isEmpty(opened)) {
      return this;
    }
    if (opened.indexOf(' ') < 1) {
      return setOpened(new Date(Long.parseLong(opened)));
    } else {
      return setOpened(fmt.parseDateTime(opened).toDate());
    }
  }

  public Site setOpened(Date opened) {
    this.opened = opened;
    return this;
  }

  @JsonGetter("@DateClosed")
  public String getClosedAsString() {
    if (null == closed) {
      return "";
    }
    return fmt.print(closed.getTime());
  }

  @JsonIgnore
  public Date getClosed() {
    return closed;
  }

  @JsonSetter("@DateClosed")
  public Site setClosed(String closed) {
    if (StringUtils.isEmpty(closed)) {
      return this;
    }
    if (closed.indexOf(' ') < 1) {
      return setClosed(new Date(Long.parseLong(closed)));
    } else {
      return setClosed(fmt.parseDateTime(closed).toDate());
    }
  }

  public Site setClosed(Date closed) {
    this.closed = closed;
    return this;
  }

  @JsonGetter("@Latitude")
  public String getLatitudeAsString() {
    return String.valueOf(getLatitude());
  }

  @JsonIgnore
  public float getLatitude() {
    return latitude;
  }

  @JsonSetter("@Latitude")
  public Site setLatitude(String latitude) {
    if (StringUtils.isEmpty(latitude)) {
      return this;
    }
    return setLatitude(Float.parseFloat(latitude));
  }

  public Site setLatitude(float latitude) {
    this.latitude = latitude;
    return this;
  }

  @JsonGetter("@Longitude")
  public String getLongitudeAsString() {
    return String.valueOf(getLongitude());
  }

  @JsonIgnore
  public float getLongitude() {
    return longitude;
  }

  @JsonSetter("@Longitude")
  public Site setLongitude(String longitude) {
    if (StringUtils.isEmpty(longitude)) {
      return this;
    }
    return setLongitude(Float.parseFloat(longitude));
  }

  public Site setLongitude(float longitude) {
    this.longitude = longitude;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Site site = (Site) o;

    return code.equals(site.code);

  }

  @Override
  public int hashCode() {
    return code.hashCode();
  }

  @Override
  public String toString() {
    return "Site{" +
           "code='" + code + '\'' +
           ", name='" + name + '\'' +
           ", opened=" + opened +
           ", closed=" + closed +
           ", latitude=" + latitude +
           ", longitude=" + longitude +
           '}';
  }

}
