package com.tugalsan.api.socket.server;

import com.tugalsan.api.function.client.maythrowexceptions.unchecked.TGS_FuncMTU_In1;
import com.tugalsan.api.log.server.TS_Log;

import com.tugalsan.api.string.client.TGS_StringUtils;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncWait;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncLst;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import java.io.*;
import java.net.*;

public class TS_SocketClient {

    final private static TS_Log d = TS_Log.of(TS_SocketServer.class);

    private TS_SocketClient(TS_ThreadSyncTrigger killTrigger, int port, TGS_FuncMTU_In1<String> onReply) {
        this.killTrigger_wt = TS_ThreadSyncTrigger.of(d.className(), killTrigger);
        this.port = port;
        this.onReply = onReply;
    }
    final public TS_ThreadSyncTrigger killTrigger_wt;
    final public int port;
    final public TGS_FuncMTU_In1<String> onReply;
    final private TS_ThreadSyncLst<String> queue = TS_ThreadSyncLst.ofSlowRead();

    public static TS_SocketClient of(TS_ThreadSyncTrigger killTrigger, int port, TGS_FuncMTU_In1<String> onReply) {
        return new TS_SocketClient(killTrigger, port, onReply);
    }

    public void add(String line) {
        if (line == null) {
            return;
        }
        queue.add(line);
    }

    public TS_SocketClient start() {
        try (var socket = new Socket("localhost", port)) {
            var out = new PrintWriter(socket.getOutputStream(), true);
            var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (killTrigger_wt.hasNotTriggered()) {
                TS_ThreadSyncWait.milliseconds20();
                var line = queue.removeAndPopFirst();
                if (TGS_StringUtils.cmn().isNullOrEmpty(line)) {
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
