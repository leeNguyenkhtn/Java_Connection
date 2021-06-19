/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TCPServer;

import cfg.Const;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author ACER
 */
public class TCPServer_PrivateChat {

    private HashMap<String, Socket> mapUsers = new HashMap<>();
    private ServerSocket serverPrivateChat;
    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final int maxClient = Const.MAX_CLIENT;
    private int Port = Const.PRIVATE_CHAT_PORT;
    private ExecutorService excuExecutorService;
    
      public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    void addUser(String name, Socket socket) {
        mapUsers.put(name, socket);
        support.firePropertyChange("list", "add", name);
    }

    void removeUser(String name) {
        mapUsers.remove(name);
        support.firePropertyChange("list", "remove", name);
    }

    //Start Client Handler
    private class ClientHandler implements Runnable {

        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String name;

        private ClientHandler(Socket socket) {
            this.socket = socket;
        }

        private synchronized void sendPrivateMessage(String destinationName, String message) throws IOException {
            if (mapUsers.containsKey(destinationName)) {
                Socket destinationSocket = mapUsers.get(destinationName);
                PrintWriter out = new PrintWriter(destinationSocket.getOutputStream(), true);
                out.println(message);
            }

        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                while (true) {
                    String msg = in.readLine();
                    StringTokenizer st = new StringTokenizer(msg, Const.DIM_CHARACTER);
                    if (msg.equals(Const.LOG_IN + Const.DIM_CHARACTER)) {
                        return;
                    } else if (st.nextToken().equals(Const.LOG_IN)) {

                        synchronized (mapUsers) {
                            String newUserName = st.nextToken();
                            if (!mapUsers.containsKey(msg)) {
                                name = newUserName;

                                break;
                            } else {
                                out.println(Const.ERROR_NAME);
                            }
                        }
                    }

                }
                //
                //
                broadcastMessage(Const.ADD + Const.DIM_CHARACTER + name);
                System.out.println(name + " has joined");
                //
                if (!mapUsers.isEmpty()) {
                    String listUser = Const.LIST;
                    for (String key : mapUsers.keySet()) {
                        listUser += Const.DIM_CHARACTER + key;
                    }
                    addUser(name, socket);
                    sendPrivateMessage(name, listUser);
                }
                else{
                    addUser(name, socket);
                }
                //
                String message;
                while ((message = in.readLine()) != null) {
                    if (!message.isEmpty()) {
                        try {
                            StringTokenizer msgTokenizer = new StringTokenizer(message, Const.DIM_CHARACTER);
                            String msgType = msgTokenizer.nextToken();
                            switch (msgType) {
                                case Const.LOG_OUT:
                                    System.out.println("Handler log out message");
                                    removeUser(name);
                                    broadcastMessage(Const.REMOVE + Const.DIM_CHARACTER + name);
                                    break;
                                case Const.SEND:
                                    String sender = msgTokenizer.nextToken();
                                    String reciever = msgTokenizer.nextToken();
                                    System.out.println("Handler send message");
                                    if (mapUsers.keySet().contains(reciever)) {
                                        sendPrivateMessage(reciever, message);
                                    } else {
                                        String offline = Const.SEND + Const.DIM_CHARACTER + reciever
                                                + Const.DIM_CHARACTER + sender + Const.DIM_CHARACTER + reciever + " " + Const.OFFLINE;
                                        sendPrivateMessage(sender, offline);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } catch (IOException e) {
                            System.err.println("Handler Message Error " + e);
                        }
                    }

                }
            } catch (IOException e) {
                System.err.println("Handler Read/Write Error " + e);
            }
        }
    }

    public synchronized void broadcastMessage(String message) throws IOException {
        for (Socket socket : mapUsers.values()) {
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
            pw.println(message);
        }
    }

    public void start() {
        try {
            {
                serverPrivateChat = new ServerSocket(Port);
                excuExecutorService = Executors.newFixedThreadPool(maxClient);
                System.out.println("Server private chat start on port " + Port);
                while (true) {
                    if (mapUsers.size() < maxClient) {
                        Runnable newUserSocket = new ClientHandler(serverPrivateChat.accept());
                        System.out.println("Accept client");
                        excuExecutorService.execute(newUserSocket);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Start Error: " + e);
        }
    }

    public void stop() throws IOException {
        try {
            if (!serverPrivateChat.isClosed()) {
                support.firePropertyChange("list","removeAll","");
                serverPrivateChat.close();
            }
            excuExecutorService.shutdown();
            mapUsers.clear();
        } catch (IOException e) {
            System.err.println("Stop Error: " + e);
            
        }
    }

    /*public static void main(String[] args) {
        TCPServer_PrivateChat server = new TCPServer_PrivateChat();
        server.start();
    }*/
}

