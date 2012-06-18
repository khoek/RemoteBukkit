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
package me.escortkeel.remotebukkit.gui;

import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Keeley Hoek (escortkeel)
 */
public class Main {

    public static final int MAJOR = 1;
    public static final int MINOR = 1;
    public static final int BUILD = 2;

    public static void main(String[] args) throws IOException {
        System.out.println("Launching RemoteBukkit GUI Client v" + MAJOR + "." + MINOR + "." + BUILD + "!");
        System.out.println();
        System.out.println("By Keeley Hoek (escortkeel)");
        System.out.println();

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
        } catch (InstantiationException ex) {
        } catch (IllegalAccessException ex) {
        } catch (UnsupportedLookAndFeelException ex) {
        }

        StartDialog sd = new StartDialog();

        if (args.length > 0) {
            if (args.length == 1) {
                if (args[0].equals("--help")) {
                    printHelpAndExit(0);
                } else {
                    System.out.println("Incorrect Argument Syntax!");
                    printHelpAndExit(1);
                }
            } else if (args.length == 3) {
                String[] hostAndPort = args[0].split(":");

                if (hostAndPort.length != 2 || args[0].isEmpty() || args[1].isEmpty()) {
                    System.out.println("Incorrect Argument Syntax!");
                    printHelpAndExit(1);
                }

                try {
                    Integer.parseInt(hostAndPort[1]);
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, hostAndPort[1] + " is not a valid 32-bit integer.", "Error", JOptionPane.ERROR_MESSAGE);

                    System.exit(1);
                }

                sd.getHost().setText(hostAndPort[0]);
                sd.getPort().setText(hostAndPort[1]);
                sd.getUsername().setText(args[1]);
                sd.getPassword().setText(args[2]);

                sd.setVisible(true);

                sd.launchGUI();

            } else {
                System.out.println("Incorrect Argument Syntax!");
                printHelpAndExit(1);
            }
        } else {
            sd.setVisible(true);
        }
    }

    private static void printHelpAndExit(int exitCode) {
        System.out.println("Run the GUI with no arguments to open the Login Dialog.");
        System.out.println("Run the GUI with the following arguments and it will attempt to use the supplied parameters to login automatically:");
        System.out.println();
        System.out.println("Use: [hostname:ip] [user] [pass] <switches>");
        System.out.println();
        System.out.println("Switches:");
        System.out.println("--help       Prints this help message.");

        System.exit(exitCode);
    }
}