package com.akvnsolutions.operationtiming_android;

public class Data {

    String operationid,orderstyle;
    long timediff;

    public Data(String operationid, String orderstyle, long timediff) {
        this.operationid = operationid;
        this.orderstyle = orderstyle;
        this.timediff = timediff;
    }

    public String getOperationid() {
        return operationid;
    }

    public void setOperationid(String operationid) {
        this.operationid = operationid;
    }

    public String getOrderstyle() {
        return orderstyle;
    }

    public void setOrderstyle(String orderstyle) {
        this.orderstyle = orderstyle;
    }

    public long getTimediff() {
        return timediff;
    }

    public void setTimediff(long timediff) {
        this.timediff = timediff;
    }
}
