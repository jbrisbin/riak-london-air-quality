package londonair.ingest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import londonair.AirQualityData;
import londonair.Site;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import reactor.rx.Stream;
import reactor.rx.Streams;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import static reactor.rx.Streams.from;

/**
 * Created by jbrisbin on 11/2/15.
 */
@SpringBootApplication
@EnableBinding(Source.class)
@ComponentScan(basePackages = {"londonair"})
public class IngestApp {

  private static final Logger LOG = LoggerFactory.getLogger(IngestApp.class);

  private static final String SITES_URL =
      "http://api.erg.kcl.ac.uk/AirQuality/Information/MonitoringSites/GroupName=All/Json";

  private static final TypeRef<List<Site>>           SITES_TYPE = new TypeRef<List<Site>>() {
  };
  private static final TypeRef<List<AirQualityData>> DATA_TYPE  = new TypeRef<List<AirQualityData>>() {
  };

  @Value("${londonair.ingest.startDate}")
  private String startDate;
  @Value("${londonair.ingest.toDate}")
  private String toDate;
  @Value("http://api.erg.kcl.ac.uk/AirQuality/Data/Site/SiteCode=%s/StartDate=${londonair.ingest.startDate}/EndDate=${londonair.ingest.toDate}/Json")
  private String allSpeciesUrl;

  @Autowired
  private ObjectMapper mapper;
  @Autowired
  private Source       source;

  @Bean
  public Configuration jsonPathConfig() {
    return Configuration.builder()
                        .jsonProvider(new JacksonJsonProvider(mapper))
                        .mappingProvider(new JacksonMappingProvider(mapper))
                        .build();
  }

  @Bean
  public Stream<AirQualityData> ingester() throws IOException {
    List<Site> sites = JsonPath.parse(new URL(SITES_URL).openStream(), jsonPathConfig())
                               .read("$.Sites.Site.*", SITES_TYPE);

    return from(sites)
        .flatMap(
            site -> {
              String url = String.format(allSpeciesUrl, site.getCode());
              if (LOG.isInfoEnabled()) {
                LOG.info("Fetching data from {}", url);
              }

              return Streams.create(sub -> {
                try (InputStream is = new URL(url).openStream();
                     java.util.stream.Stream<AirQualityData> str = JsonPath.parse(is, jsonPathConfig())
                                                                           .read("$.AirQualityData.Data.*", DATA_TYPE)
                                                                           .stream()) {
                  str.filter(data -> data.getValue() > 0)
                     .map(data -> data.setSite(site))
                     .onClose(sub::onComplete)
                     .forEach(sub::onNext);
                } catch (IOException e) {
                  throw new IllegalStateException(e);
                }
              });
            }
        );
  }

  public static void main(String... args) throws IOException {
    ApplicationContext ctx = SpringApplication.run(IngestApp.class);
    IngestApp          app = ctx.getBean(IngestApp.class);

    MessageChannel out = app.source.output();

    app.ingester()
       .consume(data -> {
         try {
           out.send(MessageBuilder.withPayload(app.mapper.writeValueAsBytes(data)).build());
         } catch (JsonProcessingException e) {
           app.LOG.error(e.getMessage(), e);
         }
       });
  }

}
