/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TCPClient;

import cfg.Const;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ACER
 */
public class TCPClient_PrivateChat {

    private Socket clientSocket;
    private String userName;
    private List<String> onlineUsers = new ArrayList<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    //Observer Pattern
    private PropertyChangeSupport support;

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    public void addOnlineUser(String name) {
        onlineUsers.add(name);
        support.firePropertyChange("onlineUsers", "add", name);
    }

    public void removerOfflineUser(String name) {
        onlineUsers.remove(name);
        support.firePropertyChange("onlineUsers", "remove", name);
    }

    public void listOnlineUsers(List<String> listUsers) {
        for (String user : listUsers) {
            onlineUsers.add(user);
        }
        support.firePropertyChange("onlineUsers", "list", listUsers);
    }

    public void receiveMessage(String sender, String message) {
        support.firePropertyChange("message", sender, message);
    }

    //
    public TCPClient_PrivateChat(String userName, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.support = new PropertyChangeSupport(this);
        this.userName = userName;
    }

    private class Listener implements Runnable {

        private BufferedReader in;

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(message);
                    if (!message.isEmpty()) {
                        synchronized (message) {
                            StringTokenizer msgTokenizer = new StringTokenizer(message, Const.DIM_CHARACTER);
                            String msgType = msgTokenizer.nextToken();
                            switch (msgType) {
                                case Const.SEND:
                                    String sender = msgTokenizer.nextToken();
                                    String receiver = msgTokenizer.nextToken();
                                    String content = msgTokenizer.nextToken();
                                    receiveMessage(sender, content);
                                    System.out.println(sender + " : " + content);
                                    break;
                                case Const.LIST:
                                    List<String> listUsers = new ArrayList<>();
                                    while (msgTokenizer.hasMoreTokens()) {
                                        String user = msgTokenizer.nextToken();
                                        listUsers.add(user);
                                    }
                                    for (String element : listUsers) {
                                        System.out.println(element);
                                    }
                                    listOnlineUsers(listUsers);
                                    break;
                                case Const.ADD:
                                    String user = msgTokenizer.nextToken();
                                    System.out.println(Const.ADD + " : " + user);
                                    addOnlineUser(user);
                                    break;
                                case Const.REMOVE:
                                    String removeUser = msgTokenizer.nextToken();
                                    System.out.println(Const.REMOVE + " : " + removeUser);
                                    removerOfflineUser(removeUser);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                System.err.println("Listener Error: " + ex);
            }
        }
    }

    private class Writer implements Runnable {

        private String message;
        private PrintWriter out;

        public Writer(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            try {
                this.out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println(message);
                System.out.println(message);
            } catch (IOException ex) {
                System.err.println("Writer Error: " + ex);
            }
        }
    }

    public void sendMessage(String receiver, String content) {
        String message = Const.SEND + Const.DIM_CHARACTER + userName
                + Const.DIM_CHARACTER + receiver + Const.DIM_CHARACTER + content;
        Runnable writeThread = new Writer(message);
        executorService.execute(writeThread);
    }

    public void loginMessage() {
        Runnable writeThread = new Writer(Const.LOG_IN + Const.DIM_CHARACTER + this.userName);
        executorService.execute(writeThread);
    }

    public void logoutMessage() {
        Runnable writeThread = new Writer(Const.LOG_OUT);
        executorService.execute(writeThread);
        support.firePropertyChange("list", "removeAll", userName);
    }

    public void start() {
        this.loginMessage();
        executorService.execute(new Listener());
    }

    public void stop() {

        try {
            logoutMessage();
            Thread.sleep(200);
            clientSocket.close();
        } catch (InterruptedException ex) {
            Logger.getLogger(TCPClient_PrivateChat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TCPClient_PrivateChat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*public static void main(String[] args) {
        try {
            Socket cliSocket = new Socket(Const.HOST_NAME, Const.PRIVATE_CHAT_PORT);
            TCPClient_PrivateChat client = new TCPClient_PrivateChat("Nguyen", cliSocket);
            client.start();
            while (true) {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Receiver");
                String receiver = scanner.nextLine();
                System.out.println("Message");
                String message = scanner.nextLine();
                client.sendMessage(receiver, message);
            }
        } catch (IOException ex) {
            Logger.getLogger(TCPClient_PrivateChat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }*/
}
