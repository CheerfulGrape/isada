package org.bmstu.ISaDA;

public class Main {
    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("rabbitmq");
        factory.setPassword("rabbitmq");
        factory.setVirtualHost("/");
        factory.setHost("127.0.0.1");
        factory.setPort(5672);

        try {
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            taskController = new TaskController(channel);
        } catch (Exception e) {
            log.error(e);
            return;
        }

        Document doc = taskController.getUrl(site);
        if (doc != null) {
            log.info("Обрабатываю страницу \"" + doc.title() + "\"");
            ParseNews(doc);
        }
    }
}
