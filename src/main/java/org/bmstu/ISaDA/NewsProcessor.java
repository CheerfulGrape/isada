package org.bmstu.ISaDA;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class NewsProcessor extends DefaultConsumer {
    private static final Logger log = LogManager.getLogger();

    public NewsProcessor(Channel channel) {
        super(channel);
    }

    private boolean ProcessArticle(String articleData)
    {
        try {
            NewsArticle article = NewsArticle.Deserialize(articleData);
            log.info("Обрабатываю текст новости: \"" + article.GetTitle() + "\"");
        } catch (ParseException | NoSuchAlgorithmException e) {
            log.error(e);
            return false;
        }
        return true;
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
