package com.example.iot_server.domain;

public class CommandReq {
    private String command; // "ON" 또는 "OFF"가 들어옴

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
}