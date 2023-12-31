package com.tugalsan.api.socket.server;

import com.tugalsan.api.runnable.client.TGS_RunnableType1;
import com.tugalsan.api.string.client.TGS_StringUtils;
import com.tugalsan.api.thread.server.TS_ThreadWait;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TS_SocketClient {

    private TS_SocketClient(TS_ThreadSyncTrigger killTrigger, int port, TGS_RunnableType1<String> onReply) {
        this.killTrigger = killTrigger;
        this.port = port;
        this.onReply = onReply;
    }
    final public TS_ThreadSyncTrigger killTrigger;
    final public int port;
    final public TGS_RunnableType1<String> onReply;
    final private ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue();

    public static TS_SocketClient of(TS_ThreadSyncTrigger killTrigger, int port, TGS_RunnableType1<String> onReply) {
        return new TS_SocketClient(killTrigger, port, onReply);
    }

    public void addToQueue(String line) {
        if (line == null) {
            return;
        }
        queue.offer(line);
    }

    public TS_SocketClient start() {
        TGS_UnSafe.run(() -> {
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
            }
        }, e -> e.printStackTrace());
        return this;
    }
}
