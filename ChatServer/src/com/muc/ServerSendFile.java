package com.muc;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class ServerSendFile extends Thread{
    private Socket socket;
    private String fileDirectory;
    private String sendTo;
    private String login;
    private DataOutputStream serverOut;
    private BufferedReader bufferedIn;
    private Socket clientSocket;
    private final int BUFFER_SIZE = 8192;

    public ServerSendFile(String login, String sendTo, String fileDirectory, Socket clientSocket)
    {
        this.clientSocket = clientSocket;
        this.login = login;
        this.sendTo = sendTo;
        this.fileDirectory = fileDirectory;
    }

    @Override
    public void run() {
        try{
            String serverName = clientSocket.getInetAddress().getHostName();
            int serverPort = clientSocket.getPort();
            socket = new Socket(serverName,serverPort);
            serverOut = new DataOutputStream(socket.getOutputStream());

            File file = new File(fileDirectory);

            int fileLength = (int) file.length();
            int fileSize = (int)Math.ceil(fileLength / BUFFER_SIZE);
            serverOut.writeUTF("sendFile "+ file.getName() +" "+ fileSize +" "+ sendTo +" "+ login);

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
            socket.close();
            this.socket.close();
        } catch (IOException e) {
        }
    }
}
