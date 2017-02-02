package com.docdoku.server.indexer;


import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author Morgan Guimard
 */
public class IndexerClientProducer {

    private static final Logger LOGGER = Logger.getLogger(IndexerClientProducer.class.getName());

    @Inject
    private IndexerConfigManager indexerConfigManager;

    @Produces
    @ApplicationScoped
    public Client createClient() {

        Settings settings = Settings.builder()
                .put("cluster.name", indexerConfigManager.getClusterName()).build();

        String host = indexerConfigManager.getHost();
        Integer port = indexerConfigManager.getPort();

        try {
            InetSocketTransportAddress address = new InetSocketTransportAddress(InetAddress.getByName(host), port);
            return new PreBuiltTransportClient(settings).addTransportAddress(address);
        } catch (UnknownHostException e) {
            LOGGER.log(Level.SEVERE, "Cannot initialize ElasticSearch client, please verify configuration", e);
            return null;
        }
    }
}
