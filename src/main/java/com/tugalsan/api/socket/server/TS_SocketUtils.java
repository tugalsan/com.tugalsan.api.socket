package com.tugalsan.api.socket.server;

import com.tugalsan.api.unsafe.client.*;
import java.net.*;

public class TS_SocketUtils {

    public static boolean available(int port) {
        return TGS_UnSafe.call(() -> {
            try (var ss = new ServerSocket(port)) {
                ss.setReuseAddress(true);
                try (var ds = new DatagramSocket(port);) {
                    ds.setReuseAddress(true);
                    return true;
                }
            }
        }, e -> false);
    }
}
