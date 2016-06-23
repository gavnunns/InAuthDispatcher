package com.inauth.PoJos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by gavnunns on 6/21/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistrationResponse implements InAuthResponse {
    public String deviceResponse;
    public RegistrationRequest registrationRequest;
    public DeviceInfo deviceInfo;
    public String newRegistration;

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public byte[] getDeviceResponseInBytes() {
        return deviceResponse.getBytes();
    }
}
