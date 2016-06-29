package com.inauth.domain;

import javax.persistence.*;

/**
 * Created by gavnunns on 6/28/16.
 */
@Entity
public class InAuthResponseDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Version
    private Integer version;
    @Lob
    @Column
    private String message;
    private String permanentId;
    private String requestType;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getPermanentId() {
        return permanentId;
    }

    public void setPermanentId(String permanentId) {
        this.permanentId = permanentId;
    }
}
