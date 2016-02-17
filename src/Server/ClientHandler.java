/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Shared.Log;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kristian Nielsen
 */
public class ClientHandler implements Runnable, Observer {

    private String name;
    private Socket s;
    private Server server;
    private ArrayList<ClientHandler> clientList = new ArrayList<ClientHandler>();
    PrintWriter pw;
    boolean stop;

    public ClientHandler(Socket s, Server server) {
        this.s = s;
        this.server = server;
    }

    public String getName() {
        return name;
    }

    public void handle() throws IOException {

        Scanner scan;
        stop = false;
        pw = new PrintWriter(s.getOutputStream(), true);
        scan = new Scanner(s.getInputStream());

        try {

            name = scan.nextLine();
            while (!name.contains("USER#") || name.length() < 6) {
                name = scan.nextLine();
            }
            name = name.substring(name.indexOf("#") + 1);

            server.addObserver(this);
            server.addToList(this);
            Logger.getLogger(Log.LOG_NAME).log(Level.INFO, "Client " + name + " connected");

            while (!stop) {

                String line = scan.nextLine();
                lineHandler(line, pw);

            }
        } catch (NoSuchElementException e2) {
            if (name == null) {
                name = "UNKNOWN";
            }
            Logger.getLogger(Log.LOG_NAME).log(Level.SEVERE, "Lost connection with client: " + name);

        } finally {
            server.deleteObserver(this);
            server.removeClientFromList(this);
            s.close();
        }
    }

    private String arrayListToString(ArrayList<ClientHandler> clientList) {
        String res = "";
        boolean firstIndex = true;
        for (ClientHandler c : clientList) {
            if (!firstIndex) {
                res += ",";
            }
            res += c.getName();
            firstIndex = false;
        }
        return res;
    }

    private void lineHandler(String line, PrintWriter pw) {
        int n = line.indexOf("#");
        if (n < 0) {
            return;
        }
        String cmd = line.substring(0, n);
        switch (cmd) {
            case "LOGOUT":
                stop = true;
                break;
            case "SEND":
                String res = line.substring(n + 1);
                int n2 = res.indexOf("#");
                String recep = res.substring(0, n2);
                String msg = res.substring(n2 + 1);
                Logger.getLogger(Log.LOG_NAME).log(Level.INFO, name + " is sending message: '" + msg + "' to: " + recep);
                server.msgClient(this, msg, findRecepFromString(recep));
                break;
        }
    }

    @Override
    public void run() {
        try {
            handle();

        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class
                    .getName()).log(Level.SEVERE, "Something went wrong", ex);
        }
    }

    @Override
    public void update(Observable o, Object o1) {
        Logger.getLogger(Log.LOG_NAME).log(Level.INFO, "clientList Updated");
        clientList = (ArrayList<ClientHandler>) o1;
        pw.println("USERS#" + arrayListToString(clientList));
    }

    private ArrayList<ClientHandler> findRecepFromString(String recep) {
        ArrayList<ClientHandler> res = new ArrayList<ClientHandler>();
        int n = recep.indexOf(",");
        if (recep.equals("*")) {
            return clientList;
        }
        if (n < 0) {
            res.add(getClientByName(recep));
            return res;
        }

        while (n > 0) {
            String name = recep.substring(0, n);
            res.add(getClientByName(name));
            recep = recep.substring(n + 1);
            n = recep.indexOf(",");
        }

        res.add(getClientByName(recep));
        return res;
    }

    public ClientHandler getClientByName(String name) {
        for (ClientHandler ch : clientList) {
            if (ch.getName().equals(name)) {
                return ch;
            }
        }
        // OBS: Denne metode crasher muligvis systemet :)
        return null;
    }

    public void getMsg(ClientHandler ch, String msg) {
        pw.println("MESSAGE#" + ch.getName() + "#" + msg);
    }

}
