package com.muc;

import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static java.sql.DriverManager.getConnection;

public class ChatClient {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private BufferedReader bufferedIn;
    private String login;


    public String getServerName() {
        return serverName;
    }

    public int getServerPort() {
        return serverPort;
    }

    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();
    private ArrayList<TopicUsageListener> topicUsageListeners = new ArrayList<>();
    private ArrayList<FileAlertListener> fileAlertListeners = new ArrayList<>();
    private ArrayList<FileConfirmListener> fileConfirmListeners = new ArrayList<>();

    public String getLogin() {
        return login;
    }

    public ChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public static void main(String[] args) throws IOException {

        ChatClient client = new ChatClient("localhost", 8818);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("ONLINE: " + login);
            }

            @Override
            public void offline(String login) {
                System.out.println("OFFLINE: " + login);
            }
        });

        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String fromLogin, String msgBody) {
                System.out.println("You got a message from " + fromLogin + "==>" + msgBody);
            }
        });

        if (!client.connect()) {
            System.err.println("Connect failed.");
        } else {
            System.out.println("Connect successful");
            if (client.login("a", "1")) {
                System.out.println("Login successful");
                client.msg("b", "Hello World!");
            } else {
                System.err.println("Login failed");
            }
            //client.logoff();
        }
    }

    public void msg(String sendTo, String msgBody) throws IOException {
        String cmd = "msg " + sendTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
    }

    public void logoff() throws IOException {
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());
    }

    public boolean login(String login, String password) throws IOException {
        String cmd = "login " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());
        this.login = login;
        String response = bufferedIn.readLine();
        System.out.println("Response Line: " + response);
        if ("ok login".equalsIgnoreCase(response)) {
            startMessageReader();
            return true;
        } else
            return false;
    }

    public void joinTopic(String topic) throws IOException {
        String cmd = "join #" + topic + "\n";
        serverOut.write(cmd.getBytes());

    }

    public boolean register(String login, String password) throws IOException {
        String cmd = "register " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response Line: " + response);
        if ("ok register".equalsIgnoreCase(response)) {
            return true;
        } else
            return false;
    }

    private void startMessageReader() {
        Thread t = new Thread() {
            @Override
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }

    private void readMessageLoop() {
        try {
            String line;
            while ((line = bufferedIn.readLine()) != null) {
                String[] tokens = StringUtils.split(line);
                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    if ("online".equalsIgnoreCase(cmd)) {
                        handleOnline(tokens);
                    } else if ("offline".equalsIgnoreCase(cmd)) {
                        handleOffline(tokens);
                    } else if ("msg".equalsIgnoreCase(cmd)) {
                        String[] tokensMsg = StringUtils.split(line, null, 3);
                        handleMessage(tokensMsg);
                    } else if ("join".equalsIgnoreCase(cmd)) {
                        handleJoin(tokens);
                    } else if ("fileAlert".equalsIgnoreCase(cmd)) {
                        handleFileAlert(tokens);
                    } else if ("fileConfirm".equalsIgnoreCase(cmd)) {
                        handleFileConfirm(tokens);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleFileConfirm(String[] tokens) throws IOException {
        String isSend = tokens[2];
        String sendTo = tokens[1];
        if (isSend.equalsIgnoreCase("yes")) {
            for (FileConfirmListener fileConfirmListener : fileConfirmListeners) {
                fileConfirmListener.onFileConfirm(sendTo);
            }

        }
    }

    private void handleFileAlert(String[] tokens) throws IOException {
        String login = tokens[1];
        String fileName = tokens[2];

        for (FileAlertListener fileAlertListener : fileAlertListeners) {
            fileAlertListener.onFileAlert(login, fileName);
        }

    }

    private void handleMessage(String[] tokensMsg) {
        String login = tokensMsg[1];
        String msgBody = tokensMsg[2];

        for (MessageListener listener : messageListeners) {
            listener.onMessage(login, msgBody);
        }
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for (UserStatusListener listener : userStatusListeners) {
            listener.offline(login);
        }
    }

    private void handleJoin(String[] tokens) {
        String topic = tokens[1];
        for (TopicUsageListener listener : topicUsageListeners) {
            listener.join(topic);
        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for (UserStatusListener listener : userStatusListeners) {
            listener.online(login);
        }
    }

    public void sendFileAlert(String sendTo, String fileName) throws IOException {
        String cmd = "sendFileAlert " + sendTo + " " + fileName + "\n";
        serverOut.write(cmd.getBytes());
    }

    public boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort);
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addUserStatusListener(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }

    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    public void addFileAlertListener(FileAlertListener listener) {
        fileAlertListeners.add(listener);
    }
    public void addFileConfirmListener(FileConfirmListener listener) {
        fileConfirmListeners.add(listener);
    }
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    public void joinTopicListener(TopicUsageListener listener) {
        topicUsageListeners.add(listener);
    }

    public void leaveTopicListener(TopicUsageListener listener) {
        topicUsageListeners.remove(listener);
    }


    public void sendFileConfirm(String login, boolean isSend) throws IOException {
        String cmd = null;
        if (isSend) {
            cmd = "sendFileConfirm " + login + " yes\n";
        } else {
            cmd = "sendFileConfirm " + login + " no\n";
        }
        serverOut.write(cmd.getBytes());

    }
}
