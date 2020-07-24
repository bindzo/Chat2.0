package com.muc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private final int serverPort;
    private ArrayList<ServerWorker> workerList = new ArrayList<>();
    private ArrayList<ServerSendFile> fileList = new ArrayList<>();

    ServerSocket serverSocket;
    ServerSocket newServerSocket;


    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    public List<ServerWorker> getWorkerList() {
        return workerList;
    }

    public List<ServerSendFile> getSendFileList() {
        return fileList;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(serverPort);
            newServerSocket = new ServerSocket(serverPort+1);
            while (true) {
                System.out.println("About to accept client connection...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);
                ServerWorker worker = new ServerWorker(this, clientSocket);
                workerList.add(worker);
                worker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removerWorker(ServerWorker serverWorker) {
        workerList.remove(serverWorker);
    }

    public void acceptSendFile(String login, String sendTo, String fileName) throws IOException {
        Socket clientFileSocket = newServerSocket.accept();

        ServerSendFile sendFile = new ServerSendFile(login, sendTo, fileName, this, clientFileSocket);
        fileList.add(sendFile);
        sendFile.start();
    }

    public void acceptReceiveFile(String login, String sendTo, String fileName) throws IOException {
        Socket clientFileSocket = newServerSocket.accept();
        ServerSendFile receiveFile = new ServerSendFile(login, sendTo, fileName, this, clientFileSocket);
        fileList.add(receiveFile);
    }
}
