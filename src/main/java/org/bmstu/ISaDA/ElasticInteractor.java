package org.bmstu.ISaDA;

import co.elastic.clients.elasticsearch._types.ErrorCause;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;

class ElasticBulkCreator extends Thread {
    private static final Logger log = LogManager.getLogger();

    private final ElasticsearchClient client;
    private final ArrayList<NewsArticle> articles;
    private final String indexName;

    public ElasticBulkCreator(ElasticsearchClient client, ArrayList<NewsArticle> articles, String indexName)
    {
        this.client = client;
        this.articles = new ArrayList<>(articles);
        this.indexName = indexName;
        articles.clear();
    }

    @Override
    public void run()
    {
        BulkRequest.Builder br = new BulkRequest.Builder();

        // Creates and indexes documents only if they do not exist
        for (NewsArticle article : articles) {
            br.operations(op -> op
                    .create(idx -> idx
                            .index(this.indexName)
                            .id(article.getHash())
                            .document(article)
                    )
            );
        }

        BulkResponse result = null;
        try {
            log.info("Запускаю индексацию статей!");
            synchronized (client) {
                result = client.bulk(br.build());
            }
            if (result.errors()) {
                boolean isError = false;
                for (BulkResponseItem item: result.items()) {
                    if (item.error() != null) {
                        if (item.error().reason() != null) {
                            if (item.error().reason().contains("document already exists")) {
                                // not en error, ignoring existing documents
                                continue;
                            }
                        }

                        if (!isError) {
                            log.error("Ошибка во время индексации статей!");
                            isError = true;
                        }
                        log.error(item.error().reason());
                    }
                }
            } else {
                log.info("Индексация успешно завершена!");
            }
        } catch (IOException e) {
            log.error("Ошибка во время индексации статей!");
            log.error(e);
        }
    }
}

class ElasticInteractor {
    private static final Logger log = LogManager.getLogger();
    private static final Integer maxQueueCount = 10;

    // config
    private String host = "";
    private String indexName = "";
    private Integer port = 0;
    private String username = "";
    private String password = "";

    private final ElasticsearchClient client;
    private final ArrayList<NewsArticle> articleQueue = new ArrayList<NewsArticle>();

    public ElasticInteractor()
    {
        loadConfig();
        this.client = initializeClient(this.username, this.password, this.host, this.port);
    }

    public NewsArticle getById(String id)
    {
        GetResponse<NewsArticle> response = null;
        try {
            synchronized (client) {
                response = client.get(g -> g.index(this.indexName).id(id), NewsArticle.class);
            }
        } catch (IOException e) {
            log.error("Ошибка во время поиска!");
            log.error(e);
            return null;
        }
        if (response.found()) {
            return response.source();
        } else {
            return null;
        }
    }

    public boolean enqueueArticle(NewsArticle article)
    {
        synchronized (articleQueue)
        {
            articleQueue.add(article);
            if (articleQueue.size() >= maxQueueCount)
            {
                new ElasticBulkCreator(this.client, this.articleQueue, this.indexName).start();
            }
        }
        return true;
    }

    private void loadConfig()
    {
        Config conf = ConfigFactory.load().getConfig("es");
        this.host = conf.getString("host");
        this.indexName = conf.getString("indexName");
        this.port = conf.getInt("port");
        this.username = conf.getString("username");
        this.password = conf.getString("password");
    }

    private static ElasticsearchClient initializeClient(String username, String password, String host, int port)
    {
        final CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    httpClientBuilder.disableAuthCaching();
                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                });

        RestClient restClient = builder.build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }
}
