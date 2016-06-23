package com.inauth.PoJos;

/**
 * Created by gavnunns on 6/21/16.
 */
public class InAuthMobileRequestInfo {
    public String message;
    public String deviceIpAddress;

    public InAuthMobileRequestInfo(String message, String deviceIpAddress) {
        this.message = message;
        this.deviceIpAddress = deviceIpAddress;
    }
}
