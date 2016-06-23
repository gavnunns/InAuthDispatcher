package com.inauth.PoJos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by gavnunns on 6/22/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogResponse implements InAuthResponse {
    public String deviceResponse;
    public DeviceInfo deviceInfo;

    @Override
    public byte[] getDeviceResponseInBytes() {
        return deviceResponse.getBytes();
    }
}
