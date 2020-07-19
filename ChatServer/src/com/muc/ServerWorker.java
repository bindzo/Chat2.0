package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static java.sql.DriverManager.getConnection;

public class ServerWorker extends Thread {
    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;
    private HashSet<String> topicSet = new HashSet<>();

    private static String DB_URL = "jdbc:mysql://localhost:3306/chat";
    private static String USER_NAME = "root";
    private static String PASSWORD = "1234";

    private Statement statement;
    private Connection connection;
    private ResultSet resultSets;

    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;

    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException, SQLException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = StringUtils.split(line);
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                if ("logoff".equals(cmd) || "quit".equalsIgnoreCase(cmd)) {
                    handleLogoff();
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                } else if ("msg".equals(cmd)) {
                    String[] tokensMsg = StringUtils.split(line, null, 3);
                    handleMessage(tokensMsg);
                } else if ("join".equalsIgnoreCase(cmd)) {
                    handleJoin(tokens);
                } else if ("leave".equalsIgnoreCase(cmd)) {
                    handleLeave(tokens);
                } else if ("register".equalsIgnoreCase(cmd)) {
                    handleRegister(tokens);
                } else {
                    String msg = "unknown " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }
        clientSocket.close();
    }


    private void handleLeave(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }

    public boolean isMemberOfTopic(String topic) {
        return topicSet.contains(topic);
    }

    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.add(topic);
        }
    }

    //format: "msg" "login" body...
    //format: "msg" "#topic" body...
    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String body = tokens[2];

        boolean isTopic = sendTo.charAt(0) == '#';

        List<ServerWorker> workerList = server.getWorkerList();

        for (ServerWorker worker : workerList) {
            if (isTopic) {
                if (worker.isMemberOfTopic(sendTo)) {
                    String outMsg = "msg " + sendTo + "-" + login + ": " + body + "\n";
                    worker.send(outMsg);
                }
            } else {
                if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                    String outMsg = "msg " + login + ": " + body + "\n";
                    worker.send(outMsg);
                }
            }
        }
    }

    private void handleLogoff() throws IOException {
        server.removerWorker(this);
        List<ServerWorker> workerList = server.getWorkerList();

        String onlineMsg = "offline " + login + "\n";

        for (ServerWorker worker : workerList) {
            if (!login.equals(worker.getLogin())) {
                worker.send(onlineMsg);
            }
        }
        clientSocket.close();
    }

    public String getLogin() {
        return login;
    }

    private boolean connectDB() {
        try {
            this.connection = getConnection(DB_URL, USER_NAME, PASSWORD);
            this.statement = connection.createStatement();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    private boolean getLoginList() {
        if (connectDB()) {
            try {
                this.resultSets = this.statement.executeQuery("select * from nguoidung");

                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    private boolean addLoginList(String login, String password) {
        if (connectDB()) {
            try {
                this.statement.executeUpdate("INSERT INTO nguoidung(tendangnhap, matkhau) VALUES (\"" + login + "\",\"" + password + "\")");
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    private boolean checkLogin(String login, String password) throws SQLException {
        if (getLoginList()) {
            while (resultSets.next()) {
                if (resultSets.getString(1).equalsIgnoreCase(login) && resultSets.getString(2).equalsIgnoreCase(password)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkLoginAvailable(String login) throws SQLException {
        if (getLoginList()) {
            while (resultSets.next()) {
                if (resultSets.getString(1).equalsIgnoreCase(login)) {

                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private void handleRegister(String[] tokens) throws SQLException, IOException {
        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];
            if (checkLoginAvailable(login) && addLoginList(login, password)) {

                String msg = "ok register\n";
                outputStream.write(msg.getBytes());
            } else {
                String msg = "error register\n";
                outputStream.write(msg.getBytes());
            }
            this.connection.close();
        }
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException, SQLException {
        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];
            if (checkLogin(login, password)) {
                String msg = "ok login\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User logged in succesfully: " + login);
                List<ServerWorker> workerList = server.getWorkerList();

                for (ServerWorker worker : workerList) {
                    if (worker.getLogin() != null) {
                        if (!login.equals(worker.getLogin())) {
                            String msg2 = "online " + worker.getLogin() + "\n";
                            send(msg2);
                        }
                    }
                }

                String onlineMsg = "online " + login + "\n";

                for (ServerWorker worker : workerList) {
                    if (!login.equals(worker.getLogin())) {
                        worker.send(onlineMsg);
                    }
                }
            } else {
                String msg = "error login\n";
                outputStream.write(msg.getBytes());
            }
            this.connection.close();
        }
    }

    private void send(String msg) throws IOException {
        outputStream.write(msg.getBytes());
    }
}
