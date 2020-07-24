package com.muc;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class SendFile extends Thread{
    private Socket socket;
    private String fileDirectory;
    private String sendTo;
    private String login;
    private DataOutputStream serverOut;
    private BufferedReader bufferedIn;
    private ChatClient client;
    private final int BUFFER_SIZE = 8192;

    public SendFile(String login, String sendTo, String fileDirectory,ChatClient client)
    {
        this.client = client;
        this.login = login;
        this.sendTo = sendTo;
        this.fileDirectory = fileDirectory;
    }

    @Override
    public void run() {
        try{

            File file = new File(fileDirectory);

            int fileLength = (int) file.length();
            int fileSize = (int)Math.ceil(fileLength / BUFFER_SIZE);
            this.client.getServerOut().write(("sendFile "+ file.getName() +" "+ fileSize +" "+ sendTo +" "+ login+"\n").getBytes());

            String serverName = client.getServerName();
            int serverPort = client.getServerPort()+1;
            socket = new Socket(serverName,serverPort);
            serverOut = new DataOutputStream(socket.getOutputStream());

            InputStream input = new FileInputStream(file);
            OutputStream output = socket.getOutputStream();
            BufferedInputStream bis = new BufferedInputStream(input);

            byte[] buffer = new byte[BUFFER_SIZE];
            int count;
            while((count = bis.read(buffer)) > 0){
                output.write(buffer, 0, count);
            }

            JOptionPane.showMessageDialog(null, "Send file successful.!");

            output.flush();
            bis.close();
            input.close();
            output.close();
            this.socket.close();
        } catch (IOException e) {
        }
    }
}
