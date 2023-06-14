package org.bmstu.ISaDA;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import com.rabbitmq.client.MessageProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bmstu.ISaDA.NewsPageParser;
import org.bmstu.ISaDA.StartPageParser;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Main {
    private static final Logger log = LogManager.getLogger();

    private static final String exchangeName = "";
    private static final String startPageUrl = "start_page_url";     // pages which host links to several news
    private static final String newsPageUrl = "news_page_url";       // pages that store 1 full article
    private static final String processNewsUrl = "process_news_url"; // news themselves to be processed

    private static final String baseSiteAddr = "https://lenta.ru";
    private static final String startPagePart = "/parts/news/";

    public static void publishToRMQ(Channel channel, String exchange, String queueUrl, String message) {
        byte[] messageBodyBytes = message.getBytes();
        try {
            channel.basicPublish(
                    exchange,
                    queueUrl,
                    false,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    messageBodyBytes
            );
        } catch (Exception e) {
            log.error(e);
        }
    }

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("rabbitmq");
        factory.setPassword("rabbitmq");
        factory.setVirtualHost("/");
        factory.setHost("127.0.0.1");
        factory.setPort(5672);

        Connection conn = factory.newConnection();
        Channel startingChannel = conn.createChannel();
        startingChannel.queueDeclare(startPageUrl, true, false, false, null);
        startingChannel.queueDeclare(newsPageUrl, true, false, false, null);
        startingChannel.queueDeclare(processNewsUrl, true, false, false, null);
        for (int i = 1; i < 100; ++i) {
            String url = baseSiteAddr + startPagePart + Integer.toString(i);
            publishToRMQ(startingChannel, exchangeName, startPageUrl, url);
        }
        startingChannel.close();

        Channel startPageChannel = conn.createChannel();
        startPageChannel.basicConsume(startPageUrl, false, "startPageParser1", new StartPageParser(startPageChannel, exchangeName, newsPageUrl, baseSiteAddr));

        Channel newsPageChannel = conn.createChannel();
        newsPageChannel.basicConsume(newsPageUrl, false, "newsPageParser1", new NewsPageParser(newsPageChannel, exchangeName, processNewsUrl));

        Channel articleProcessChannel = conn.createChannel();
        articleProcessChannel.basicConsume(processNewsUrl, false, "articleProcessor1", new NewsProcessor(articleProcessChannel));
    }
}
