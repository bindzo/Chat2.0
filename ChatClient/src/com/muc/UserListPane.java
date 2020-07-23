package com.muc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class UserListPane extends JPanel implements UserStatusListener, TopicUsageListener {
    private final ChatClient client;
    private JList<String> userListUI;
    private DefaultListModel<String> userListModel;
    JTextField topicField;
    JButton joinTopic;

    public UserListPane(ChatClient client) {
        this.client = client;
        this.client.addUserStatusListener(this);
        this.client.joinTopicListener(this);
        userListModel = new DefaultListModel<>();
        userListUI = new JList<>(userListModel);
        setLayout(new BorderLayout());
        add(new JScrollPane(userListUI), BorderLayout.CENTER);

        topicField = new JTextField();
        joinTopic = new JButton("Join Topic");

        joinTopic.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String topic = topicField.getText();
                try {
                    client.joinTopic(topic);
                    topicField.setText("");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

            }
        });
        userListUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    String login = userListUI.getSelectedValue();
                    MessagePane messagePane = new MessagePane(client, login);

                    JFrame f = new JFrame("Message: " + login);
                    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    f.setSize(500, 500);
                    f.getContentPane().add(messagePane, BorderLayout.CENTER);
                    f.setVisible(true);
                }
            }
        });
    }

    public void logoffOnExit() {
        try {
            this.client.logoff();
            this.setVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void initComponents() {
        JFrame frame = new JFrame("User List");

        JPanel topicPanel = new JPanel();
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                logoffOnExit();
            }
        });
        frame.setSize(400, 600);
        frame.getContentPane().add((this), BorderLayout.CENTER);

        frame.add(topicPanel, BorderLayout.SOUTH);
        topicPanel.setLayout(new BoxLayout(topicPanel, BoxLayout.Y_AXIS));
        topicPanel.add(topicField);
        topicPanel.add(joinTopic);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient("localhost", 8818);
        UserListPane userListPane = new UserListPane(client);
        JFrame frame = new JFrame("User List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 600);

        frame.getContentPane().add((userListPane), BorderLayout.CENTER);
        frame.setVisible(true);

        if (client.connect()) {
            try {
                client.login("a", "a");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void online(String login) {
        userListModel.addElement(login);
    }

    @Override
    public void offline(String login) {
        userListModel.removeElement(login);
    }


    @Override
    public void join(String topic) {
        userListModel.addElement(topic);
    }

    @Override
    public void leave(String topic) {
        userListModel.removeElement(topic);
    }
}
