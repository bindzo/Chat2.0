package com.muc;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class ServerSendFile extends Thread{
    private Socket socket;
    private String fileDirectory;
    private String sendTo;
    private String login;
    private OutputStream outputStream;
    private BufferedReader bufferedIn;
    private Socket clientSocket;
    private final int BUFFER_SIZE = 8192;

    public String getSendTo() {
        return sendTo;
    }

    public String getLogin() {
        return login;
    }

    private final Server server;

    public ServerSendFile(String login, String sendTo, String fileDirectory,Server server,Socket clientSocket) throws IOException {
        this.server =server;
        this.clientSocket = clientSocket;
        this.login = login;
        this.sendTo = sendTo;
        this.fileDirectory = fileDirectory;
        this.outputStream = clientSocket.getOutputStream();

    }
    private void send(byte[] buffer, int cnt) throws IOException {
        outputStream.write(buffer, 0, cnt);
    }

    @Override
    public void run() {
        try{
            InputStream inputStream = clientSocket.getInputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int cnt;
            List<ServerSendFile> sendFileList = this.server.getSendFileList();
            while ((cnt = inputStream.read(buffer)) > 0) {
                for (ServerSendFile sendFile : sendFileList){
                    if(sendFile.getLogin().equalsIgnoreCase(this.getSendTo())){
                        sendFile.send(buffer,cnt);
                    }
                }
            }
            this.outputStream.flush();
            this.outputStream.close();
        } catch (IOException e) {
        }
    }
}
