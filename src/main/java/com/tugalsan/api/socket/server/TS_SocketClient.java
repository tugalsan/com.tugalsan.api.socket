package com.tugalsan.api.socket.server;

import com.tugalsan.api.callable.client.TGS_CallableType1_Run;
import com.tugalsan.api.log.server.TS_Log;

import com.tugalsan.api.string.client.TGS_StringUtils;
import com.tugalsan.api.thread.server.TS_ThreadWait;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TS_SocketClient {

    final private static TS_Log d = TS_Log.of(TS_SocketServer.class);

    private TS_SocketClient(TS_ThreadSyncTrigger killTrigger, int port, TGS_CallableType1_Run<String> onReply) {
        this.killTrigger = killTrigger;
        this.port = port;
        this.onReply = onReply;
    }
    final public TS_ThreadSyncTrigger killTrigger;
    final public int port;
    final public TGS_CallableType1_Run<String> onReply;
    final private ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue();

    public static TS_SocketClient of(TS_ThreadSyncTrigger killTrigger, int port, TGS_CallableType1_Run<String> onReply) {
        return new TS_SocketClient(killTrigger, port, onReply);
    }

    public void addToQueue(String line) {
        if (line == null) {
            return;
        }
        queue.offer(line);
    }

    public TS_SocketClient start() {
        try (var socket = new Socket("localhost", port)) {
            var out = new PrintWriter(socket.getOutputStream(), true);
            var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (killTrigger.hasNotTriggered()) {
                TS_ThreadWait.milliseconds20();
                var line = queue.poll();
                if (TGS_StringUtils.isNullOrEmpty(line)) {
                    continue;
                }
                out.println(line);
                out.flush();
                onReply.run(in.readLine());
            }
        } catch (IOException ex) {
            d.ct("start", ex);
        }
        return this;
    }
}
