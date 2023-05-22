package org.bmstu.crawler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {
    private static final Logger log = LogManager.getLogger();
    private static TaskController taskController = null;

    private static final String baseSiteAddr = "https://lenta.ru";
    private static final String newsAddr = "/parts/news";

    private static final String site = "https://lenta.ru/parts/news/100";

    static public void ParseNews(Document doc) {
        try {
            Elements news = doc.getElementsByClass("parts-page__body _parts-news").select("li.parts-page__item"); //.select("parts-page__item");
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
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    public static void main(String[] args) {
        taskController = new TaskController();
        Document doc = taskController.getUrl(site);
        if (doc != null) {
            log.info("Обрабатываю страницу \"" + doc.title() + "\"");
            ParseNews(doc);
        }
    }

}