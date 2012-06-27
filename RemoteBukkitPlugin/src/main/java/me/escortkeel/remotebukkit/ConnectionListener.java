/*
 * Copyright (c) 2012, Keeley Hoek
 * All rights reserved.
 * 
 * Redistribution and use of this software in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 *   Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 * 
 *   Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package me.escortkeel.remotebukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Keeley Hoek (escortkeel@live.com)
 */
public class ConnectionListener extends Thread {

    private final RemoteBukkitPlugin plugin;
    private final ServerSocket s;

    public ConnectionListener(RemoteBukkitPlugin plugin, int port) {
        this.plugin = plugin;
        
        try {
            s = new ServerSocket(port);
        } catch (IOException ex) {
            RuntimeException e = new RuntimeException("Failed to listen on port:" + port);
            e.addSuppressed(ex);

            throw e;
        }
    }

    @Override
    public void run() {
        while (true) {
            Socket sock;
            try {
                sock = s.accept();

                try {
                    ConnectionHandler handler = new ConnectionHandler(plugin, sock);

                    String user = null;
                    String pass = null;

                    try {
                        BufferedReader out = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                        user = out.readLine();
                        pass = out.readLine();
                    } catch (IOException ex) {
                        RuntimeException e = new RuntimeException("Could not read credentials from client, killing!");
                        e.addSuppressed(ex);

                        try {
                            handler.kill("Could not read credentials!");
                        } catch (IOException ed) {
                        }

                        throw e;
                    }

                    if (user.equals(plugin.getConfig().get("user")) && pass.equals(plugin.getConfig().get("pass"))) {
                        plugin.didAcceptConnection(handler);
                    } else {
                        handler.send("Incorrect Credentials.");

                        try {
                            handler.kill("Incorrect Credentials.");
                        } catch (IOException ex) {
                        }
                    }
                } catch (IOException ex) {
                    RuntimeException e = new RuntimeException("Could not read credentials from client, killing!");
                    e.addSuppressed(ex);

                    throw e;
                }
            } catch (IOException ex) {
            }
        }
    }

    public void kill() {
        try {
            s.close();
        } catch (IOException ex) {
        }
    }
}
