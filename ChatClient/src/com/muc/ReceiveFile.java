package com.muc;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class ReceiveFile extends Thread{
    private final String fileName;
    private Socket socket;
    private String fileDirectory;
    private String sendTo;
    private String login;
    private DataOutputStream serverOut;
    private BufferedReader bufferedIn;
    private ChatClient client;
    private final int BUFFER_SIZE = 8192;

    public ReceiveFile(String login, String sendTo, String fileDirectory,String fileName,ChatClient client)
    {
        this.fileName = fileName;
        this.client = client;
        this.login = login;
        this.sendTo = sendTo;
        this.fileDirectory = fileDirectory;
    }

    @Override
    public void run() {
        try{
            String serverName = client.getServerName();
            int serverPort = client.getServerPort()+1;
            socket = new Socket(serverName,serverPort);
            String path = fileDirectory + "\\" + fileName;
            FileOutputStream fos = new FileOutputStream(path);
            fos.flush();
            InputStream input = socket.getInputStream();

            BufferedInputStream bIS = new BufferedInputStream(input);
            byte[] buffer = new byte[BUFFER_SIZE];
            int count;
            while((count = bIS.read(buffer)) != -1){
                fos.write(buffer, 0, count);

            }
            bIS.close();
            fos.flush();
            fos.close();
        } catch (IOException e) {
        }
    }
}
