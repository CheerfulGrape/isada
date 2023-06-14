package org.bmstu.ISaDA;

import co.elastic.clients.util.DateTime;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.commons.codec.digest.DigestUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

public class NewsArticle {
    static public int MonthToNumber(String month)
    {
        switch (month) {
            case "января": return 1;
            case "февраля": return 2;
            case "марта": return 3;
            case "апреля": return 4;
            case "мая": return 5;
            case "июня": return 6;
            case "июля": return 7;
            case "августа": return 8;
            case "сентября": return 9;
            case "октября": return 10;
            case "ноября": return 11;
            case "декабря": return 12;
            default: return 1;
        }
    }

    static public DateTime StringToLocalDateTime(String timestamp)
    {
        // "17:11, 14 июня 2023"
        String time = timestamp.split(", ")[0];
        String date = timestamp.split(", ")[1];

        int year = Integer.parseInt(date.split(" ")[2]);
        int month = MonthToNumber(date.split(" ")[1]);
        int day = Integer.parseInt(date.split(" ")[0]);

        int hour = Integer.parseInt(time.split(":")[0]);
        int minute = Integer.parseInt(time.split(":")[1]);

        DateTime x = DateTime.of(LocalDateTime.of(year, month, day, hour, minute).toString());

        return DateTime.of(LocalDateTime.of(year, month, day, hour, minute).toString());
    }

    public NewsArticle(String title, String date, String text, String author, String url) {
        _title = title;
        _date = StringToLocalDateTime(date).getString();
        _text = text;
        _author = author;
        _url = url;
    }

    public NewsArticle(String title, String date, String text, String author, String url, boolean convertDate) {
        _title = title;
        if (convertDate) {
            _date = StringToLocalDateTime(date).getString();
        } else {
            _date = date;
        }
        _text = text;
        _author = author;
        _url = url;
    }

    public static NewsArticle deserialize(String jsonString) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(jsonString);

        String title = (String)json.get("title");
        String date = (String)json.get("date");
        String text = (String)json.get("text");
        String author = (String)json.get("author");
        String url = (String)json.get("url");

        return new NewsArticle(title, date, text, author, url, false);
    }

    public String serialize() {
        String title = _title.replace("\"", "\\\"").replace("\n", "\\n");
        String date = _date.replace("\"", "\\\"").replace("\n", "\\n");
        String text = _text.replace("\"", "\\\"").replace("\n", "\\n");
        String author = _author.replace("\"", "\\\"").replace("\n", "\\n");
        String url = _url.replace("\"", "\\\"").replace("\n", "\\n");

        return String.format("{\"title\": \"%s\",\"date\": \"%s\",\"text\": \"%s\",\"author\": \"%s\",\"url\": \"%s\"}", title, date, text, author, url);
    }

    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        _title = title;
    }

    public String getDate() {
        return _date;
    }

    public void setDate(String date) {
        _date = date;
    }

    public String getText() {
        return _text;
    }

    public void setText(String text) {
        _text = text;
    }

    public String getAuthor() {
        return _author;
    }

    public void setAuthor(String author) {
        _author = author;
    }

    public String getUrl() {
        return _url;
    }

    public void setUrl(String url) {
        _url = url;
    }

    public String getHash() {
        String hashedParts = _title + _date + _author;
        return DigestUtils.md5Hex(hashedParts).toUpperCase();
    }

    private String _title;
    private String _date;
    private String _text;
    private String _author;
    private String _url;
}
