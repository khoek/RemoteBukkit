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

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class RemoteBukkitPlugin extends JavaPlugin {

    private static final Logger log = Logger.getLogger("Minecraft-Server");
    private static final ArrayList<String> oldMsgs = new ArrayList<>();
    private String username;
    private String password;

    public static void log(String msg) {
        log.log(Level.INFO, "[RemoteBukkit] " + msg);
    }

    public static void log(String msg, IOException ex) {
        log.log(Level.INFO, "[RemoteBukkit] " + msg, ex);
    }
    private LogHandler handler;
    private ArrayList<ConnectionHandler> connections = new ArrayList<>();
    private ConnectionListener listener;

    @Override
    public void onLoad() {
        getConfig().options().copyDefaults(true);
    }

    @Override
    public void onEnable() {
        handler = new LogHandler(this);

        log.log(Level.INFO, getDescription().getFullName().concat(" is enabled! By Keeley Hoek (escortkeel)"));
        log.addHandler(handler);

        Object rawUsername = getConfig().get("user");
        if(rawUsername instanceof String) {
            username = ((String) rawUsername);
        } else if(rawUsername instanceof Integer) {
            username = ((Integer) rawUsername).toString();
        } else {
            log.log(Level.WARNING, "[RemoteBukkit] Illegal or no username specified, defaulting to \"username\"");
            username = "username";
        }

        Object rawPassword = getConfig().get("pass");
        if(rawPassword instanceof String) {
            password = ((String) rawPassword);
        } else if(rawPassword instanceof Integer) {
            password = ((Integer) rawPassword).toString();
        } else {
            log.log(Level.WARNING, "[RemoteBukkit] Illegal or no password specified, defaulting to \"password\"");
            password = "password";
        }
        
        int port = getConfig().getInt("port");
        if (port <= 1024) {
            log.log(Level.WARNING, "[RemoteBukkit] Illegal or no port specified, using default port 25564");

            port = 25564;
        }

        listener = new ConnectionListener(this, port);
        listener.start();

        saveConfig();
    }

    @Override
    public void onDisable() {
        log.removeHandler(handler);

        listener.kill();

        for (ConnectionHandler con : new ArrayList<>(connections)) {
            con.kill("Plugin is being disabled!");
        }
    }

    public void broadcast(String msg) {
        synchronized (oldMsgs) {
            oldMsgs.add(msg);

            for (ConnectionHandler con : new ArrayList<>(connections)) {
                con.send(msg);
            }
        }
    }

    public void didEstablishConnection(ConnectionHandler con, Directive directive) {
        RemoteBukkitPlugin.log("Connection #" + con.getNumber() + " from " + con.getSocket().getInetAddress().getHostAddress() + ":" + con.getSocket().getPort() + " was successfully established.");

        connections.add(con);

        if (directive == Directive.NOLOG) {
            con.send("Connection was successfully established.");
        } else {
            synchronized (oldMsgs) {
                for (String msg : oldMsgs) {
                    con.send(msg);
                }
            }
        }

        con.setDirective(directive);
        con.start();
    }

    public void didCloseConnection(ConnectionHandler con) {
        RemoteBukkitPlugin.log("Connection #" + con.getNumber() + " from " + con.getSocket().getInetAddress().getHostAddress() + ":" + con.getSocket().getPort() + " was closed.");

        connections.remove(con);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
