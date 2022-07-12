package com.niochat.client;

import java.io.IOException;

public class ClientB {
    public static void main(String[] args) {
        try {
            new ChatClient().startClient("Marry");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
