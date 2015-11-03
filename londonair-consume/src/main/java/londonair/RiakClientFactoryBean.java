package londonair;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.core.RiakCluster;
import com.gs.collections.impl.list.mutable.FastList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by jbrisbin on 10/29/15.
 */
@Component
public class RiakClientFactoryBean implements FactoryBean<RiakClient>, Lifecycle {

  private final Logger        log     = LoggerFactory.getLogger(getClass());
  private final AtomicBoolean started = new AtomicBoolean(false);

  @Value("${londonair.riak.hosts:localhost}")
  private String hosts;

  private RiakClient client;


  @Override
  public RiakClient getObject() throws Exception {
    FastList<InetSocketAddress> addrs = FastList.newList();
    for (String host : StringUtils.commaDelimitedListToSet(hosts)) {
      String[] parts = StringUtils.delimitedListToStringArray(host, ":");
      addrs.add(new InetSocketAddress(parts[0], Integer.valueOf(parts.length == 2 ? parts[1] : "8087")));
      if (log.isDebugEnabled()) {
        log.debug("Using {} as host connection...", Arrays.toString(parts));
      }
    }
    return (client = RiakClient.newClient(addrs.toTypedArray(InetSocketAddress.class)));
  }

  @Override
  public Class<?> getObjectType() {
    return RiakClient.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  @Override
  public void start() {
    if (!started.compareAndSet(false, true)) {
      return;
    }
    if (log.isDebugEnabled()) {
      log.debug("Connected to {} nodes", riakClusterToString(client.getRiakCluster()));
    }
  }

  @Override
  public void stop() {
    if (started.compareAndSet(true, false)) {
      try {
        client.shutdown().get(120, TimeUnit.SECONDS);
      } catch (Throwable t) {
        throw new IllegalStateException(t);
      }
    }
  }

  @Override
  public boolean isRunning() {
    return started.get();
  }

  private static String riakClusterToString(RiakCluster cluster) {
    StringBuilder sb = new StringBuilder();
    cluster.getNodes().forEach(n -> {
      sb.append("[")
        .append(n.getRemoteAddress())
        .append(":")
        .append(n.getPort())
        .append(",")
        .append(n.getNodeState())
        .append("] ");
    });
    return sb.toString();
  }

}
