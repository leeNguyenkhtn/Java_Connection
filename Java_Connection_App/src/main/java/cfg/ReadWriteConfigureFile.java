/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cfg;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import javafx.util.Pair;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ACER
 */
public class ReadWriteConfigureFile {
    private HashMap<String,Pair<String,Integer>> mapServer = new HashMap(); 
    private String serverFileConfigure;
    public static final String serverPrivateChat = "serverPrivateChat";
    public static final String serverGroupChat = "serverGroupChat";
    public static final String serverSendFile = "serverSendFile";
    public ReadWriteConfigureFile()
    {
        serverFileConfigure = "DefaultServerFileConfigure.txt";
    }
    public ReadWriteConfigureFile(String serverFileConfigure)
    {
        this.serverFileConfigure = serverFileConfigure;
    }
    public void loadConfigure()
    {
        try {
            BufferedReader buf = new BufferedReader(new FileReader(serverFileConfigure));
            String line;
            while((line = buf.readLine())!=null)
            {
                
                String[] arrStrings = line.split("-");
                mapServer.put(arrStrings[0], new Pair<>(arrStrings[1],Integer.parseInt(arrStrings[2])));
            }
            buf.close();
        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (IOException ex) {
            Logger.getLogger(ReadWriteConfigureFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public Pair<String,Integer> getServerConfigure(String serverName)
    {
        return mapServer.get(serverName);
    }
    public void adjustServerConfigure(String serverName ,String hostName,int port)
    {
        mapServer.put(serverName, new Pair<>(hostName, port));
        try {
            PrintWriter pw = new PrintWriter(serverFileConfigure);
            Set setOfServer = mapServer.entrySet();
        Iterator i = setOfServer.iterator();
        while(i.hasNext())
        {
            Map.Entry me = (Map.Entry) i.next();
            Pair<String,Integer> pair = (Pair<String,Integer>) me.getValue();
            pw.write(me.getKey()+"-"+pair.getKey()+"-"+pair.getValue()+"\n");
        }
        pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReadWriteConfigureFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
