package com.tugalsan.api.socket.server;

import com.tugalsan.api.callable.client.TGS_CallableType1;
import com.tugalsan.api.string.client.TGS_StringUtils;
import com.tugalsan.api.thread.server.TS_ThreadWait;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import java.io.*;
import java.net.*;

public class TS_SocketServer {

    private TS_SocketServer(TS_ThreadSyncTrigger killTrigger, int port, TGS_CallableType1<String, String> forEachReceivedLine) {
        this.killTrigger = killTrigger;
        this.port = port;
        this.forEachReceivedLine = forEachReceivedLine;
    }

    public static TS_SocketServer of(TS_ThreadSyncTrigger killTrigger, int port, TGS_CallableType1<String, String> forEachReceivedLine) {
        return new TS_SocketServer(killTrigger, port, forEachReceivedLine);
    }
    final public TS_ThreadSyncTrigger killTrigger;
    final public int port;
    final public TGS_CallableType1<String, String> forEachReceivedLine;

    public void start() {
        TGS_UnSafe.run(() -> {
            try (var server = new ServerSocket(port)) {
                server.setReuseAddress(true);
                while (killTrigger.hasNotTriggered()) {
                    TS_ThreadWait.milliseconds20();
                    var clientSocket = server.accept();
//                    System.out.println("client connected" + clientSocket.getInetAddress().getHostAddress());
                    Thread.startVirtualThread(() -> {
                        TGS_UnSafe.run(() -> {
                            try (clientSocket) {
                                try (var in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); var out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                                    while (killTrigger.hasNotTriggered()) {
                                        TS_ThreadWait.milliseconds20();
                                        var line = in.readLine();
                                        if (TGS_StringUtils.isNullOrEmpty(line)) {
                                            continue;
                                        }
                                        out.println(forEachReceivedLine.call(line));
                                    }
                                }
                            }
                        }, e -> e.printStackTrace());
                    });
                }
            }
        }, e -> e.printStackTrace());
    }
}
