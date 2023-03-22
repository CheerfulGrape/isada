package org.bmstu.crawler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {
    private static final Logger log = LogManager.getLogger();
    private static TaskController taskController = null;
    private static final String site = "https://dtf.ru/";

    static public void ParseNews(Document doc) {
        try {
            Elements news = doc.getElementsByClass("").select("");
            for (Element element : news) {

            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    public static void main(String[] args) {
        taskController = new TaskController(site);
        Document doc = taskController.getUrl(site);
        String title;
        if (doc != null) {
            title = doc.title();
            log.info(title);
            ParseNews(doc);
        }
    }

}