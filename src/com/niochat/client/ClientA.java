package com.niochat.client;

import java.io.IOException;

public class ClientA {
    public static void main(String[] args) {
        try {
            new ChatClient().startClient("Lucy");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
