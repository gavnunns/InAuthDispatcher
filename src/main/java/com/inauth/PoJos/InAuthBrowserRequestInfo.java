package com.inauth.PoJos;

import java.util.Map;

/**
 * Created by gavnunns on 6/16/16.
 */
public class InAuthBrowserRequestInfo {
    public Map<String, String> httpHeaders;
    public String remoteAddress;
    public String transactionId;
    public String requestMethod;
    public String requestTimestamp;
    public String payload;

    public InAuthBrowserRequestInfo(Map<String, String> httpHeaders, String remoteAddress, String transactionId, String requestMethod, String requestTimestamp, String payload) {
        this.httpHeaders = httpHeaders;
        this.remoteAddress = remoteAddress;
        this.transactionId = transactionId;
        this.requestMethod = requestMethod;
        this.requestTimestamp = requestTimestamp;
        this.payload = payload;
    }
}
