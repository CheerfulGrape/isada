package org.bmstu.ISaDA;

import com.rabbitmq.client.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StartPageParser extends DefaultConsumer {
    private static final Logger log = LogManager.getLogger();

    private static final String exchangeName = "";
    private static final String newsPageUrl = "news_page_url";

    private final PageGetter pageGetter = new PageGetter();

    private String baseSiteAddr = "https://lenta.ru";

    public StartPageParser(Channel channel, String baseSiteAddr) {
        super(channel);
        this.baseSiteAddr = baseSiteAddr;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        long deliveryTag = envelope.getDeliveryTag();
        String url = new String(body, StandardCharsets.UTF_8);

        Document page = pageGetter.getPageFromUrl(url);
        if (page == null || !ParsePage(page)) {
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

    private boolean ParsePage(Document page) {
        try {
            Elements news = page.getElementsByClass("parts-page__body _parts-news").select("li.parts-page__item");
            for (Element element : news) {
                if (element.hasClass("parts-page__item _more _parts-news"))
                    continue;

                String name = element.getElementsByClass("card-full-news__title").text();
                String timeStamp = element.getElementsByClass("card-full-news__info-item card-full-news__date").text();
                String category = element.getElementsByClass("card-full-news__info-item card-full-news__rubric").text();
                String newsRef = element.getElementsByClass("card-full-news _parts-news").attr("href");

                if (category.isEmpty())
                    category = "Без категории";

                if (newsRef.startsWith("/"))
                    newsRef = baseSiteAddr + newsRef;
                else
                    continue;

                log.info("Нашёл новсть: \"" + name + "\" от " + timeStamp + " в категории \"" + category + "\", " + newsRef);
                publishToRMQ(newsPageUrl, newsRef);
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
