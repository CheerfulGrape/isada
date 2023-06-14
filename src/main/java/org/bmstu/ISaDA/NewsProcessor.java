package org.bmstu.ISaDA;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class NewsProcessor extends DefaultConsumer {
    private static final Logger log = LogManager.getLogger();

    private final ElasticInteractor elasticInteractor = new ElasticInteractor();

    public NewsProcessor(Channel channel) {
        super(channel);
    }

    private boolean ProcessArticle(String articleData)
    {
        try {
            NewsArticle article = NewsArticle.deserialize(articleData);
            log.info("Пытаюсь поместить статью в очередь на обработку: \"" + article.getTitle() + "\"");
            if (!elasticInteractor.enqueueArticle(article))
            {
                log.error("-> Не добавить статью в очередь на обработку!");
                return false;
            }
            else
            {
                log.info("-> Статья добавлена в очередь на обработку!");
                return true;
            }
        } catch (ParseException e) {
            log.error("Ошибка при парсинге статьи!");
            return false;
        }
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        long deliveryTag = envelope.getDeliveryTag();
        String articleData = new String(body, StandardCharsets.UTF_8);

        boolean isOk = ProcessArticle(articleData);

        if (!isOk) {
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
}
