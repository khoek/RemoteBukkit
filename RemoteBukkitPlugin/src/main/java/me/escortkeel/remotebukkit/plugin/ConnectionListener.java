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
package me.escortkeel.remotebukkit.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;

/**
 *
 * @author Keeley Hoek (escortkeel@live.com)
 */
public class ConnectionListener extends Thread {

    private final RemoteBukkitPlugin plugin;
    private final ServerSocket s;
    private int number = 0;

    public ConnectionListener(RemoteBukkitPlugin plugin, int port) {
        super("RemoteBukkit-ConnectionListener");
        this.setDaemon(true);

        this.plugin = plugin;

        try {
            s = new ServerSocket(port);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to listen on port:" + port, ex);
        }
    }

    @Override
    public void run() {
        while (true) {
            ConnectionHandler con = null;
            try {
                con = new ConnectionHandler(plugin, s.accept(), number++);

                RemoteBukkitPlugin.log("Connection #" + con.getNumber() + " from " + con.getSocket().getInetAddress().getHostAddress() + ":" + con.getSocket().getPort() + " was accepted.");

                try {
                    BufferedReader out = new BufferedReader(new InputStreamReader(con.getSocket().getInputStream()));

                    String user = out.readLine();
                    String pass = out.readLine();

                    if (plugin.getConfig().get("user").equals(user) && plugin.getConfig().get("pass").equals(pass)) {
                        String raw = out.readLine();
                        Directive directive = Directive.toDirective(raw);
                        if (directive == null) {
                            RemoteBukkitPlugin.log("Connection #" + con.getNumber() + " from " + con.getSocket().getInetAddress().getHostAddress() + ":" + con.getSocket().getPort() + " requested the use of an unsupported directive \"" + raw + "\".");
                            con.kill("Unsported directive \"" + raw + "\".");
                        } else {
                            plugin.didEstablishConnection(con, directive);
                        }
                    } else {
                        RemoteBukkitPlugin.log("Connection #" + con.getNumber() + " from " + con.getSocket().getInetAddress().getHostAddress() + ":" + con.getSocket().getPort() + " attempted to authenticate using incorrect credentials.");
                        con.kill("Incorrect credentials.");
                    }

                    continue;
                } catch (IOException ex) {
                    RemoteBukkitPlugin.log("Connection #" + con.getNumber() + " from " + con.getSocket().getInetAddress().getHostAddress() + ":" + con.getSocket().getPort() + " abruptly closed the connection during authentication.");
                }
            } catch (IOException ex) {
                try {
                    RemoteBukkitPlugin.log("Exception while attempting to accept connection #" + con.getNumber() + " from " + con.getSocket().getInetAddress().getHostAddress() + ":" + con.getSocket().getPort(), ex);
                } catch (Exception e) {
                }
            }

            try {
                con.kill();
            } catch (Exception e) {
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
