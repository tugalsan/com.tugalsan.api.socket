package com.tugalsan.api.socket.server;

import java.io.*;
import java.net.*;

public class TS_SocketUtils {

    public static boolean available(int port) {
        try (var ss = new ServerSocket(port)) {
            ss.setReuseAddress(true);
            try (var ds = new DatagramSocket(port);) {
                ds.setReuseAddress(true);
                return true;
            }
        } catch (IOException ex) {
            return false;
        }
    }
}
