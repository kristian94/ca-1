/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Shared.Log;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kristian Nielsen
 */
public class Server extends Observable {

    boolean keepRunning;
    private String ip;
    private int port;
    ServerSocket serverSock;
    private ArrayList<ClientHandler> clientList = new ArrayList<ClientHandler>();
    ServerSocket serverSocket;
   

    public void handleClient(Socket s, ClientHandler c) throws IOException {

    }

    public void runServer(String ip, int port) {
        this.port = port;
        this.ip = ip;

        keepRunning = true;
        
        Logger.getLogger(Log.LOG_NAME).log(Level.INFO, "Server Started. Listening on: " + port + ", bound to " + ip);
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(ip, port));
            do {
                Logger.getLogger(Log.LOG_NAME).log(Level.INFO, "Server Status: Ready to recieve a new client");
                Socket socket = serverSocket.accept(); //Important Blocking call
                Logger.getLogger(Log.LOG_NAME).log(Level.INFO, "Client Connected");
                ClientHandler ch = new ClientHandler(socket, this);
                Logger.getLogger(Log.LOG_NAME).log(Level.INFO, "Starting client thread...");
                new Thread(ch).start();
                

            } while (keepRunning);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {

        int port = 9999;
        String ip = "192.168.1.162";
        if (args.length == 2) {
            ip = args[0];
            port = Integer.parseInt(args[1]);
        }

        try {
            Log.setLogFile("logFile.txt", "ServerLog");
            new Server().runServer(ip, port);
        } finally {
            Log.closeLogger();
        }

    }  



    void removeClientFromList(ClientHandler ch) {
        clientList.remove(ch);
        updateList();
        Logger.getLogger(Log.LOG_NAME).log(Level.INFO, "Removed " + ch.getName() + " from list");
    }

    private void updateList() {
        setChanged();
        notifyObservers(clientList);
        Logger.getLogger(Log.LOG_NAME).log(Level.INFO, "Updating List...");
        Logger.getLogger(Log.LOG_NAME).log(Level.INFO, "Current number of observers: " + countObservers());
        
    }
    
    public void msgClient(ClientHandler ch, String msg, ArrayList<ClientHandler> recepients){
        for(ClientHandler c: recepients){
            c.getMsg(ch, msg);
        }
    }
    
    public void stopServer(){
        keepRunning = false;
    }

    public void addToList(ClientHandler ch) {
        clientList.add(ch);
        updateList();
        Logger.getLogger(Log.LOG_NAME).log(Level.INFO, "Added " + ch.getName() + " to list");
    }

    

}
