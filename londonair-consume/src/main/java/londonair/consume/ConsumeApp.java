package londonair.consume;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.timeseries.Store;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.timeseries.Cell;
import com.basho.riak.client.core.query.timeseries.Row;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.collections.impl.list.mutable.FastList;
import londonair.AirQualityData;
import londonair.AirQualityDataEnriched;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import reactor.Processors;
import reactor.core.processor.ExecutorProcessor;
import reactor.rx.Streams;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Consume messages from Kafka, batch them up, then save to Riak.
 */
@SpringBootApplication
@EnableBinding(Sink.class)
@ComponentScan(basePackages = {"londonair"})
public class ConsumeApp {

  private static final Logger LOG = LoggerFactory.getLogger(ConsumeApp.class);

  @Autowired
  private RiakClient   client;
  @Autowired
  private ObjectMapper mapper;

  @Value("${spring.cloud.stream.bindings.input}")
  private String kafkaTopic;
  @Value("${londonair.ingest.bucket.raw}")
  private String rawBucket;
  @Value("${londonair.ingest.bucket.enriched}")
  private String enrichedBucket;
  @Value("${londonair.ingest.batch.size}")
  private int    batchSize;
  @Value("${londonair.ingest.batch.timeout}")
  private int    batchTimeout;

  @Bean
  public Namespace londonAirQualityNS() {
    return isRaw() ? new Namespace(rawBucket, rawBucket) : new Namespace(enrichedBucket, enrichedBucket);
  }

  @Bean
  public ExecutorProcessor<AirQualityData, AirQualityData> riakSink() {
    ExecutorProcessor<AirQualityData, AirQualityData> p = Processors.topic("AirQualityData", 4 * 1024, true);

    Streams.wrap(p)
           .map(data -> {
             FastList<Cell> row = FastList.newList();

             row.add(new Cell(data.getMeasurementDate()));
             row.add(new Cell(data.getSite().getCode()));
             row.add(new Cell(data.getSite().getLatitude()));
             row.add(new Cell(data.getSite().getLongitude()));
             row.add(new Cell(data.getSpeciesCode()));
             row.add(new Cell(data.getValue()));

             if (data instanceof AirQualityDataEnriched) {
               AirQualityDataEnriched enriched = (AirQualityDataEnriched) data;
               row.add(new Cell(enriched.getTemperature()));
               row.add(new Cell(enriched.getWindSpeed()));
               row.add(new Cell(enriched.getWindDirection()));
               row.add(new Cell(enriched.getRelativeHumidity()));
             }

             return new Row(row);
           })
           .buffer(batchSize, batchTimeout, TimeUnit.SECONDS)
           .map(rows -> {
             try {
               if (LOG.isInfoEnabled()) {
                 LOG.info("Storing batch of size {} to {}", rows.size(), londonAirQualityNS());
               }
               return client.executeAsync(new Store.Builder(londonAirQualityNS().getBucketNameAsString())
                                              .withRows(rows)
                                              .build());
             } catch (Exception e) {
               LOG.error(e.getMessage(), e);
               return null;
             }
           })
           .consume(f -> {
             f.addListener(rf -> {
               if (rf.isSuccess()) {
                 LOG.info("Stored batch to {}", londonAirQualityNS().getBucketNameAsString());
               } else {
                 LOG.error(rf.cause().getMessage(), rf.cause());
               }
             });
           });

    return p;
  }

  @ServiceActivator(inputChannel = Sink.INPUT)
  public void messageHandler(byte[] bytes) {
    if (isRaw()) {
      riakSink().onNext(read(bytes, AirQualityData.class));
    } else {
      riakSink().onNext(read(bytes, AirQualityDataEnriched.class));
    }
  }

  private boolean isRaw() {
    return kafkaTopic.endsWith(".raw");
  }

  private <T> T read(byte[] bytes, Class<T> type) {
    try {
      return mapper.readValue(bytes, type);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static void main(String... args) {
    SpringApplication.run(ConsumeApp.class);
  }

}
