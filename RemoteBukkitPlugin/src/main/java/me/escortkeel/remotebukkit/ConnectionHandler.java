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
import java.io.PrintStream;
import java.net.Socket;

/**
 *
 * @author Keeley Hoek (escortkeel@live.com)
 */
public class ConnectionHandler extends Thread {

    private final Socket s;
    private final PrintStream out;
    private final BufferedReader in;

    public ConnectionHandler(Socket s) throws IOException {
        this.s = s;
        this.out = new PrintStream(s.getOutputStream());
        this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    @Override
    public void run() {
        while (true) {
            try {
                String input = in.readLine();
                
                if(input == null) {
                    break;
                }
                
                RemoteBukkitPlugin.getInstance().getServer().dispatchCommand(RemoteBukkitPlugin.getInstance().getServer().getConsoleSender(), input);
            } catch (IOException ex) {
            }
        }
        
        try {
            kill();
        } catch (IOException ex) {
        }
    }

    public void kill() throws IOException {
        RemoteBukkitPlugin.getInstance().didCloseConnection(this);
        
        send("RemoteBukkit closing connection!");        
        s.close();
    }

    public void send(String msg) {
        out.println(msg);
    }

    public Socket getSocket() {
        return s;
    }
}
