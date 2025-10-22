package org.example.Domain.dtos;

public class RequestDto {
    private String controller;  // e.g. "Auth", "Product"
    private String request;     // e.g. "login", "register"
    private String data;        // JSON of a specific DTO (LoginRequestDto, RegisterRequestDto...)
    private String token;       // Optional: session token for authentication

    public RequestDto() {}

    public RequestDto(String controller, String request, String data, String token) {
        this.controller = controller;
        this.request = request;
        this.data = data;
        this.token = token;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
