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
package me.escortkeel.remotebukkit.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;
import jline.console.ConsoleReader;

/**
 *
 * @author Keeley Hoek (escortkeel@live.com)
 */
public class Main {

    public static final int MAJOR = 1;
    public static final int MINOR = 3;
    public static final int BUILD = 2;
    public static int SWITCHCOUNT = 1;
    private static boolean prefixLevel = false;
    private static volatile ConsoleReader console = null;

    public static synchronized ConsoleReader getConsole() throws IOException {
        if (console == null) {
            console = new ConsoleReader();
        }

        return console;
    }

    public static void main(String[] args) throws IOException {
        try {
            System.out.println("Launching RemoteBukkit Console Client v" + MAJOR + "." + MINOR + "." + BUILD + "!");
            System.out.println();
            System.out.println("By Keeley Hoek (escortkeel)");
            System.out.println();

            checkPreSwitches(args);

            if (args.length >= 3 && args.length <= 3 + SWITCHCOUNT) {
                checkSwitches(Arrays.copyOfRange(args, 3, args.length));

                Scanner sc = new Scanner(args[0]).useDelimiter(":");

                final Socket s = new Socket(sc.next(), sc.nextInt());

                Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Thread") {

                    @Override
                    public void run() {
                        try {
                            s.close();
                        } catch (IOException ex) {
                        }
                    }
                });

                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintStream out = new PrintStream(s.getOutputStream());

                InputForwardThread ift = new InputForwardThread(out);
                ift.setDaemon(true);

                out.println(args[1]);
                out.println(args[2]);

                ift.start();

                while (true) {
                    String msg = in.readLine();

                    if (msg == null) {
                        break;
                    } else {
                        if (prefixLevel) {
                            try {
                                msg = msg.split("\\[")[1].split("\\]")[0] + msg;
                            } catch (Exception e) {
                            }
                        }

                        getConsole().println('\r' + msg);
                        getConsole().flush();
                        getConsole().drawLine();
                        getConsole().flush();
                    }
                }
            } else {
                System.out.println("Incorrect Argument Syntax!");
                printHelpAndExit(1);
            }
        } catch (NoSuchElementException ex) {
            System.out.println("Incorrect Argument Syntax!");
            printHelpAndExit(1);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private static void checkPreSwitches(String[] switches) {
        for (String arg : switches) {
            if (arg.equalsIgnoreCase("--help")) {
                printHelpAndExit(0);
            }
        }
    }

    private static void checkSwitches(String[] switches) {
        for (String arg : switches) {
            if (arg.equalsIgnoreCase("--prefixlevel")) {
                prefixLevel = true;
            } else {
                System.out.println("Invalid Switch: " + arg);
                printHelpAndExit(1);
            }
        }
    }

    private static void printHelpAndExit(int exitCode) {
        System.out.println("Use: [hostname:ip] [user] [pass] <swithches>");
        System.out.println();
        System.out.println("Switches:");
        System.out.println("--help       Prints this help message.");
        System.out.println("--pefixlevel Prefixes each output message with the log level.");

        System.exit(exitCode);
    }
}
