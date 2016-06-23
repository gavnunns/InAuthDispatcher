package com.inauth.PoJos;

/**
 * Created by gavnunns on 6/21/16.
 */
public class DeviceInfo {
    public String permanentId;
    public String publicSigningKey;

    @Override
    public String toString() {
        return "Device Info {PermanentId:" + permanentId + "}";
    }
}
