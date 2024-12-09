package com.example.HeygenAPI.response;
public class Response {
    private String status;
//    private String value;

    public Response(String status){
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
