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

    private static final String exchangeName = "";
    private static final String newsPageUrl = "process_url";

    private final PageGetter pageGetter = new PageGetter();

    public NewsPageParser(Channel channel) {
        super(channel);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        long deliveryTag = envelope.getDeliveryTag();
        String url = new String(body, StandardCharsets.UTF_8);

        Document page = pageGetter.getPageFromUrl(url);
        if (page == null || !ParseNews(page)) {
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

    private boolean ParseNews(Document page) {
        try {
            Elements news = page.getElementsByClass("parts-page__body _parts-news").select("li.parts-page__item");
            for (Element element : news) {
                publishToRMQ(newsPageUrl, "");
            }
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
