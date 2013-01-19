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
import java.io.PrintStream;
import java.net.Socket;

/**
 *
 * @author Keeley Hoek (escortkeel@live.com)
 */
public class ConnectionHandler extends Thread {

    private final RemoteBukkitPlugin plugin;
    private final Socket s;
    private final BufferedReader in;
    private final PrintStream out;
    private final int number;
    private Directive directive;

    public ConnectionHandler(RemoteBukkitPlugin plugin, Socket s, int number) throws IOException {
        super("RemoteBukkit-ConnectionHandler");
        this.setDaemon(true);

        this.plugin = plugin;
        this.s = s;
        this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        this.out = new PrintStream(s.getOutputStream());
        this.number = number;
    }

    @Override
    public void run() {
        try {
            while (true) {
                final String input = in.readLine();

                if (input == null) {
                    break;
                }

                RemoteBukkitPlugin.log("Connection #" + getNumber() + " from " + s.getInetAddress().getHostAddress() + ":" + s.getPort() + " dispatched command: " + input);

                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), input);
                    }
                });
            }
        } catch (IOException ex) {
        }

        kill();
    }

    public int getNumber() {
        return number;
    }

    public Socket getSocket() {
        return s;
    }

    public Directive getDirective() {
        return directive;
    }

    public void setDirective(Directive directive) {
        this.directive = directive;
    }

    public void kill() {
        plugin.didCloseConnection(this);

        try {
            s.close();
        } catch (IOException ex) {
        }
    }

    public void kill(String reason) {
        directive = Directive.INTERACTIVE;
        
        send("\nRemoteBukkit closing connection because:");
        send(reason);

        kill();
    }

    public void send(String msg) {
        if (directive != Directive.NOLOG) {
            out.println(msg);
        }
    }
}
