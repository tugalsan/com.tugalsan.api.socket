package com.tugalsan.api.socket.server;

import module com.tugalsan.api.function;
import module com.tugalsan.api.log;
import module com.tugalsan.api.string;
import module com.tugalsan.api.thread;
import java.io.*;
import java.net.*;

public class TS_SocketServer {

    final private static TS_Log d = TS_Log.of(TS_SocketServer.class);

    private TS_SocketServer(TS_ThreadSyncTrigger killTrigger, int port, TGS_FuncMTU_OutTyped_In1<String, String> forEachReceivedLine) {
        this.killTrigger_wt = TS_ThreadSyncTrigger.of(d.className(), killTrigger);
        this.port = port;
        this.forEachReceivedLine = forEachReceivedLine;
    }

    public static TS_SocketServer of(TS_ThreadSyncTrigger killTrigger, int port, TGS_FuncMTU_OutTyped_In1<String, String> forEachReceivedLine) {
        return new TS_SocketServer(killTrigger, port, forEachReceivedLine);
    }
    final public TS_ThreadSyncTrigger killTrigger_wt;
    final public int port;
    final public TGS_FuncMTU_OutTyped_In1<String, String> forEachReceivedLine;

    public TS_SocketServer start() {
        try (var server = new ServerSocket(port)) {
            server.setReuseAddress(true);
            while (killTrigger_wt.hasNotTriggered()) {
                TS_ThreadSyncWait.milliseconds20();
                var clientSocket = server.accept();
//                    System.out.println("client connected" + clientSocket.getInetAddress().getHostAddress());
                Thread.startVirtualThread(() -> {
                    try (clientSocket) {
                        try (var in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); var out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                            while (killTrigger_wt.hasNotTriggered()) {
                                TS_ThreadSyncWait.milliseconds20();
                                var line = in.readLine();
                                if (TGS_StringUtils.cmn().isNullOrEmpty(line)) {
                                    continue;
                                }
                                out.println(forEachReceivedLine.call(line));
                            }
                        }
                    } catch (IOException ex) {
                        d.ct("start.clientSocket", ex);
                    }
                });
            }
        } catch (IOException ex) {
            d.ct("start", ex);
        }
        return this;
    }
}
