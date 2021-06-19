/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cfg;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author ACER
 */
public class Const {
    public static int MAX_CLIENT = 10;
    public static int PRIVATE_CHAT_PORT = 3200;
    public static final String HOST_NAME = "localhost";
    public static final String ERROR_NAME = "This name existed";
    public static final String SEND = "SEND";
    public static final String ADD = "ADD";
    public static final String REMOVE = "REMOVE";
    public static final String LIST = "LIST";
    public static final String LOG_OUT = "LOG_OUT";
    public static final String LOG_IN = "LOG_IN";
    public static final String DIM_CHARACTER = "|";
    public static final String OFFLINE = "is offline!!!";
    
    public static String CurrentTime()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
        Date date = new Date();  
        return date.toString();
    }
}
