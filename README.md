# RemoteBukkit
## General
RemoteBukkit allows admins to, with the help of a plugin, remotely use the server's console. You do this by running a console client on you computer and you provide this client with the hostname and port on which the RemoteBukkit plugin's server is running. Of course you will also need to provide the client with the server's username and password (set up in the plugin config file) for security.

So, provided the server has the RemoteBukkit plugin installed, admins can remotely use the Bukkit Console to control the server and execute Bukkit console commands server side.

RemoteBukkit does support multiple, simultaneous, connected clients.

## Use
### Plugin
Simply place the plugin in the standard Bukkit plugin directory and it will automatically generate it's configuration the next time the server is run. The 3 options in the plugin config file are self explanatory (username, password and port) and the defaults are:

    user: username
    pass: password
    port: 25564 

### GUI Client
RemoteBukkit offers a very simple and easy to use GUI client which you can use to connect to the plugin. Just double click on the GUI's jarfile to run it. The GUI features a self-explanatory interface where you supply the hostname, port, username and password of the RemoteBukkit server.

### Console Client
If you prefer to use a console-based application to connect to the RemoteBukkit plugin you can too! The console argument syntax is very simple but the program but will print help information if you supply no/the wrong number of/invalid arguments:

Use: [hostname:ip] [user] [pass] <switches>

**Switches**

* --help - Prints the help message.
* --pefixlevel - Prefixes each output message with the log level. 

##Download
The latest builds can be downloaded [here](http://dev.bukkit.org/server-mods/remotebukkit/files/).