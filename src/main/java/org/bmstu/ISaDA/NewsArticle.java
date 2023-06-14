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

    static public String NumberToMonth(int month)
    {
        switch (month) {
            case 1: return "января";
            case 2: return "февраля";
            case 3: return "марта";
            case 4: return "апреля";
            case 5: return "мая";
            case 6: return "июня";
            case 7: return "июля";
            case 8: return "августа";
            case 9: return "сентября";
            case 10: return "октября";
            case 11: return "ноября";
            case 12: return "декабря";
            default: return "января";
        }
    }

    static public LocalDateTime StringToLocalDateTime(String timestamp)
    {
        // "17:11, 14 июня 2023"
        String time = timestamp.split(", ")[0];
        String date = timestamp.split(", ")[1];

        int year = Integer.parseInt(date.split(" ")[2]);
        int month = MonthToNumber(date.split(" ")[1]);
        int day = Integer.parseInt(date.split(" ")[0]);

        int hour = Integer.parseInt(time.split(":")[0]);
        int minute = Integer.parseInt(time.split(":")[1]);

        return LocalDateTime.of(year, month, day, hour, minute);
    }

    static public String LocalDateTimeToString(LocalDateTime timestamp)
    {
        // "17:11, 14 июня 2023"
        return String.format("%02d:%02d, %d %s %d",
                timestamp.getHour(),
                timestamp.getMinute(),
                timestamp.getDayOfMonth(),
                NumberToMonth(timestamp.getMonth().getValue()),
                timestamp.getYear()
        );
    }

    public NewsArticle(String title, String date, String text, String author, String url) {
        _title = title;
        _date = StringToLocalDateTime(date);
        _text = text;
        _author = author;
        _url = url;

        String hashedParts = _title + _date + _author;

        _hash = DigestUtils.md5Hex(hashedParts).toUpperCase();
    }

    public static NewsArticle deserialize(String jsonString) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(jsonString);

        String title = (String)json.get("title");
        String date = (String)json.get("date");
        String text = (String)json.get("text");
        String author = (String)json.get("author");
        String url = (String)json.get("url");

        return new NewsArticle(title, date, text, author, url);
    }

    public String serialize() {
        String title = _title.replace("\"", "\\\"");
        String date = LocalDateTimeToString(_date);
        String text = _text.replace("\"", "\\\"");
        String author = _author.replace("\"", "\\\"");
        String url = _url.replace("\"", "\\\"");

        return String.format("{\"title\": \"%s\",\"date\": \"%s\",\"text\": \"%s\",\"author\": \"%s\",\"url\": \"%s\"}", title, date, text, author, url);
    }

    public String getTitle() {
        return _title;
    }

    public DateTime getDate() {
        return DateTime.of(_date.toString());
    }

    public String getText() {
        return _text;
    }

    public String getAuthor() {
        return _author;
    }

    public String getUrl() {
        return _url;
    }

    public String getHash() {
        return _hash;
    }

    private final String _title;
    private final LocalDateTime _date;
    private final String _text;
    private final String _author;
    private final String _url;

    private final String _hash;
}
