package org.bmstu.ISaDA;

import com.rabbitmq.client.Channel;
import org.apache.http.HttpEntity;

import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class PageGetter {
    private static final Logger log = LogManager.getLogger();
    private CloseableHttpClient client = null;
    private HttpClientBuilder builder = null;
    private final int retryDelay = 5 * 1000;
    private final int retryCount = 2;
    private final int metadataTimeout = 30 * 1000;

    public PageGetter() {
        CookieStore httpCookieStore = new BasicCookieStore();
        this.builder = HttpClientBuilder.create().setDefaultCookieStore(httpCookieStore);
        this.client = builder.build();
    }

    public Document getPageFromUrl(String inUrl) {
        int code = 0;
        boolean bStop = false;
        Document doc = null;
        URL url = null;

        try {
          url = new URL(inUrl);
        } catch (MalformedURLException e) {
            log.error(e);
            return doc;
        }

        for (int iTry= 0; iTry < retryCount && !bStop; ++iTry) {
            log.info("Загружаю страницу с адресом \"" + url + "\"");
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(metadataTimeout)
                    .setConnectTimeout(metadataTimeout)
                    .setConnectionRequestTimeout(metadataTimeout)
                    .setExpectContinueEnabled(true)
                    .build();

            HttpGet request = new HttpGet(url.toString());
            request.setConfig(requestConfig);
            CloseableHttpResponse response = null;

            try {
                response = client.execute(request);
                code = response.getStatusLine().getStatusCode();
                if (code == 404) {
                    log.warn("Не смог получить страницу с адресом \"" + url + "\"! Код ошибки: " + code);
                    bStop = true;
                } else if (code == 200) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        try {
                            doc = Jsoup.parse(entity.getContent(), "UTF-8", url.toString());
                            break;
                        } catch (IOException e) {
                            log.error(e);
                        }
                    }
                    bStop = true;
                } else {
                    log.warn("Не смог получить страницу с адресом \"" + url + "\"! Код ошибки: " + code);
                    response.close();
                    response = null;
                    client.close();
                    CookieStore httpCookieStore = new BasicCookieStore();
                    builder.setDefaultCookieStore(httpCookieStore);
                    client = builder.build();
                    int delay = retryDelay * (iTry + 1);
                    log.info("Попробую снова через " + delay / 1000 + " секунд...");
                    try {
                        Thread.sleep(delay);
                        continue;
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            } catch (IOException e) {
                log.error(e);
            }
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }
        return doc;
    }
}
