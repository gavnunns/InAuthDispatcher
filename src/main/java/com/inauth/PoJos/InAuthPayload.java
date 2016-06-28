package com.inauth.PoJos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by gavnunns on 6/7/16.
 */
public class InAuthPayload {
    private InAuthMobileRequestInfo mobilePayload;
    private InAuthBrowserRequestInfo browserRequestInfo;

    public InAuthPayload(InAuthMobileRequestInfo mobilePayload) {
        this.mobilePayload = mobilePayload;
    }

    public InAuthPayload(InAuthBrowserRequestInfo browserRequestInfo) {
        this.browserRequestInfo = browserRequestInfo;
    }

    public String browserRequestFormat() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(browserRequestInfo);
    }

    public String mobileRequestFormat() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(mobilePayload);
    }
}
