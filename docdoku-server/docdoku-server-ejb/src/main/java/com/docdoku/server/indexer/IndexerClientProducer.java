package com.docdoku.server.indexer;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class hold and produce elastic search client.
 * <p>
 * This class is managed by a pool size, see glassfish-ejb-jar.xml
 * Simply inject the client in your beans with `@Inject Client client;'
 *
 * @author Morgan Guimard
 */
@Stateless(name = "IndexerClientProducer")
public class IndexerClientProducer {

    private static final Logger LOGGER = Logger.getLogger(IndexerClientProducer.class.getName());

    private Client client;

    @Inject
    private IndexerConfigManager config;

    @PostConstruct
    public void open() {
        LOGGER.log(Level.INFO, "Create elasticsearch client");
        Settings settings = Settings.builder()
                .put("cluster.name", config.getClusterName()).build();

        String host = config.getHost();
        Integer port = config.getPort();

        try {
            InetSocketTransportAddress address = new InetSocketTransportAddress(InetAddress.getByName(host), port);
            client = new PreBuiltTransportClient(settings).addTransportAddress(address);
        } catch (UnknownHostException e) {
            client = new PreBuiltTransportClient(settings);
            LOGGER.log(Level.SEVERE, "Cannot initialize ElasticSearch client, please verify the resource configuration", e);
        }
    }

    @PreDestroy
    public void close() {
        if (null != client) {
            LOGGER.log(Level.INFO, "Closing elasticsearch client");
            client.close();
        } else {
            LOGGER.log(Level.INFO, "Cannot close a null client");
        }
    }

    @Produces
    @RequestScoped
    public Client produce() {
        LOGGER.log(Level.INFO, "Producing client");
        return client;
    }

}
