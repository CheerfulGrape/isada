package org.bmstu.ISaDA;

import com.rabbitmq.client.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class NewsPageParser extends DefaultConsumer {
    private static final Logger log = LogManager.getLogger();

    private final PageGetter pageGetter = new PageGetter();

    private String exchangeName = "";
    private String processNewsUrl = "";

    public NewsPageParser(Channel channel, String exchangeName, String processNewsUrl) {
        super(channel);
        this.exchangeName = exchangeName;
        this.processNewsUrl = processNewsUrl;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        long deliveryTag = envelope.getDeliveryTag();
        String url = new String(body, StandardCharsets.UTF_8);

        Document page = pageGetter.getPageFromUrl(url);
        if (page == null || !ParseNews(page, url)) {
            try {
                getChannel().basicReject(deliveryTag, false);
            } catch (IOException e) {
                log.error(e);
            }
            return;
        }

        try {
            getChannel().basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error(e);
        }
    }

    private boolean ParseNews(Document page, String url) {
        try {
            String title = page.getElementsByClass("topic-body__title").text();
            String author = page.getElementsByClass("topic-authors__author").text();
            String dateTime = page.getElementsByClass("topic-header__time").text();

            StringBuilder articleBuilder = new StringBuilder();
            Elements articleParts = page.getElementsByClass("topic-body__content").first().getElementsByClass("topic-body__content-text");
            for (Element element : articleParts) {
                articleBuilder.append(element.text()).append("\n\n");
            }
            String article = articleBuilder.toString();

            NewsArticle newsArticle = new NewsArticle(title, dateTime, article, author, url);
            publishToRMQ(processNewsUrl, newsArticle.Serialize());

            log.info("Обработал новость: \""+ title + "\", " + dateTime + ", автор - " + author + " | " + url);
        } catch (Exception e) {
            log.error(e);
            return false;
        }
        return true;
    }

    public void publishToRMQ(String queueUrl, String message) {
        byte[] messageBodyBytes = message.getBytes();
        try {
            getChannel().basicPublish(
                    exchangeName,
                    queueUrl,
                    false,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    messageBodyBytes
            );
        } catch (Exception e) {
            log.error(e);
        }
    }
}
