package org.bmstu.crawler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

public class Main {
    private static Logger log = LogManager.getLogger();
    private static TaskController taskController = null;
    private static String site = "https://mytishi.ru";

    public static void main(String[] args) {
        taskController = new TaskController(site);
        Document doc = taskController.getUrl(site);
        System.out.println("Hello world!");
    }

}