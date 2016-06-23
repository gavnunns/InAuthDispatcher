package com.inauth.workers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inauth.PoJos.InAuthPayload;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by gavnunns on 6/7/16.
 *
 */
public class InAuthRequestProcessor {
    public static final String MOBILE_REGISTER = "mobile_request";
    public static final String BROWSER_REQUEST = "browser_request";

    //Customer Browser API KEY should be config file controlled
    private static final String MOBILE_API_KEY = "e3eb70c3-c90d-4faf-b7c1-af21da998bc5";
    private static final String BROWSER_API_KEY = "5533c90db4833dd080a9e22268e7b4350ae1fe74";
    private static final String API_SERVER_URL = "https://staging-api.cdn-net.com/";
    private static final String MOBILE_API_SERVER_URL = "https://riskapi-staging-api.inauth.com/";
    private static final String BROWSER_REQUEST_ENDPOINT = "v1/browser-request?apiKey=";
    private static final String MOBILE_REGISTER_ENDPOINT = "v2/mobile/register?apiKey=";
    public static final String MOBILE_LOG = "v2/mobile/log";
    private static final String MOBILE_LOG_ENDPOINT = "v2/mobile/log?apiKey=";

    public String submitPayloadToInAuth(InAuthPayload inAuthPayload, final String type) throws IOException {
        switch (type) {
            case MOBILE_REGISTER:
                return submitMobileRegisterRequest(inAuthPayload);
            case BROWSER_REQUEST:
                return submitBrowserRequest(inAuthPayload);
            case MOBILE_LOG:
                return submitMobileLogRequest(inAuthPayload);
        }
        return "Unknown Request";
    }

    private String submitMobileLogRequest(InAuthPayload inAuthPayload) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(MOBILE_API_SERVER_URL + MOBILE_LOG_ENDPOINT + MOBILE_API_KEY);
        buildMobileRequestToInAuthAPI(inAuthPayload, httpPost);
        return sendMultipartRequestToInAuthAPI(httpclient, httpPost);
    }

    private void buildMobileRequestToInAuthAPI(InAuthPayload inAuthPayload, HttpPost httpPost) throws JsonProcessingException {
        StringBody request = new StringBody(inAuthPayload.mobileRequestFormat(), ContentType.APPLICATION_JSON);
        HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("message", request).build();
        httpPost.setEntity(reqEntity);
    }

    private String submitBrowserRequest(InAuthPayload inAuthPayload) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(API_SERVER_URL + BROWSER_REQUEST_ENDPOINT + BROWSER_API_KEY);

        StringBody request = new StringBody(inAuthPayload.browserRequestFormat(), ContentType.APPLICATION_JSON);
        HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("request", request).build();

        httpPost.setEntity(reqEntity);

        System.out.println("executing request " + httpPost.getRequestLine());

        return sendMultipartRequestToInAuthAPI(httpclient, httpPost);
    }

    private String submitMobileRegisterRequest(InAuthPayload inAuthPayload) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(MOBILE_API_SERVER_URL + MOBILE_REGISTER_ENDPOINT + MOBILE_API_KEY);
        buildMobileRequestToInAuthAPI(inAuthPayload, httpPost);

        return sendMultipartRequestToInAuthAPI(httpclient, httpPost);
    }

    private String sendMultipartRequestToInAuthAPI(CloseableHttpClient httpclient, HttpPost httpPost) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            HttpEntity resEntity = response.getEntity();
            InputStream is = resEntity.getContent();
            int i;
            char c;
            while ((i = is.read()) != -1) {
                c = (char) i;
                sb.append(c);
            }
            EntityUtils.consume(resEntity);
        }
        return sb.toString();
    }
}

