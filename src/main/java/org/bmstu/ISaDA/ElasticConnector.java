package org.bmstu.ISaDA;

import com.typesafe.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ElasticConnector {
    private static final Logger log = LogManager.getLogger();

    private Config config;
    private PreBuiltTransportClient client;

    public void initialize(Config cfg) {
        this.config = cfg;
        try {
            this.client = createClient();
        } catch (Exception e) {
            log.error(e);
        }
    }

    private PreBuiltTransportClient createClient() throws UnknownHostException {
        Settings settings = Settings.builder()
                .put("cluster.name", config.getString("cluster"))
                .build();
        PreBuiltTransportClient cli = new PreBuiltTransportClient(settings);
        cli.addTransportAddress(new TransportAddress(
                InetAddress.getByName(config.getString("host")),
                Integer.parseInt(config.getString("port"))));
        return cli;
    }
}