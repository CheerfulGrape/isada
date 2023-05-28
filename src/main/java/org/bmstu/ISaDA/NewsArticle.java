package org.bmstu.ISaDA;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class NewsArticle {

    public NewsArticle(String title, String date, String text, String author, String url) throws NoSuchAlgorithmException {
        _title = title;
        _date = date;
        _text = text;
        _author = author;
        _url = url;

        String hashedParts = _title + _date + _author;

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(hashedParts.getBytes());
        _hash = Arrays.toString(md.digest());
    }

    public static NewsArticle Deserialize(String jsonString) throws ParseException, NoSuchAlgorithmException {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(jsonString);

        String title = (String)json.get("title");
        String date = (String)json.get("date");
        String text = (String)json.get("text");
        String author = (String)json.get("author");
        String url = (String)json.get("url");

        return new NewsArticle(title, date, text, author, url);
    }

    public String Serialize() {
        return String.format("{\"title\": \"%s\",\"date\": \"%s\",\"text\": \"%s\",\"author\": \"%s\",\"url\": \"%s\"}", _title, _date, _text, _author, _url);
    }

    public String GetTitle() {
        return _title;
    }

    public String GetDate() {
        return _date;
    }

    public String GetText() {
        return _text;
    }

    public String GetAuthor() {
        return _author;
    }

    public String GetUrl() {
        return _author;
    }

    public String GetHash() {
        return _hash;
    }

    private final String _title;
    private final String _date;
    private final String _text;
    private final String _author;
    private final String _url;

    private final String _hash;
}
