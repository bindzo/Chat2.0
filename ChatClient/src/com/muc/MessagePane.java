package com.muc;

import com.vdurmont.emoji.EmojiParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;


public class MessagePane extends JPanel implements MessageListener, FileAlertListener, FileConfirmListener {

    private final String login;
    private final ChatClient client;
    private String fileName;
    private String fileDirectory;

    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> messageList = new JList<>(listModel);
    private JTextField inputField = new JTextField();
    private JButton sendFileButton = new JButton("Send File");

    public MessagePane(ChatClient client, String login) {
        this.client = client;
        this.login = login;
        client.addFileAlertListener(this);
        client.addMessageListener(this);
        client.addFileConfirmListener(this);
        setLayout(new BorderLayout());
        add(new JScrollPane(messageList), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);
        add(sendFileButton, BorderLayout.NORTH);
        sendFileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.showSaveDialog(null);
                File file = fileChooser.getSelectedFile();
                fileName = null;
                fileDirectory = null;
                if (fileChooser != null) {
                    fileName = file.getName();
                    fileDirectory = file.toString();
                }
                try {
                    client.sendFileAlert(login, fileName);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String text = inputField.getText();
                    String result = EmojiParser.parseToUnicode(text);
                    client.msg(login, text);
                    listModel.addElement("You: " + result);
                    inputField.setText("");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onMessage(String fromLogin, String msgBody) {
        msgBody = EmojiParser.parseToUnicode(msgBody);

        if (fromLogin.equalsIgnoreCase(this.login+":")|| fromLogin.charAt(0) == '#') {
            String line = fromLogin + " " + msgBody;
            listModel.addElement(line);
        }

    }

    @Override
    public void onFileAlert(String login, String fileName) throws IOException {
        int dialogButton = JOptionPane.YES_NO_OPTION;
        int dialogResult = JOptionPane.showConfirmDialog(null, "Would You Like to Save file " + fileName + " from " + login, "Warning", dialogButton);
        if (dialogResult == 0) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Choose Save Folder");
            fileChooser.showSaveDialog(null);
            File folder = fileChooser.getSelectedFile();

            String directory = folder.toString();

            this.client.sendFileConfirm(login, true);
            ReceiveFile receiveFile = new ReceiveFile(this.client.getLogin(), login, directory, fileName, client);
            receiveFile.start();

        } else {
            this.client.sendFileConfirm(login, false);
        }
    }

    @Override
    public void onFileConfirm(String login) {
        SendFile sendFile = new SendFile(this.client.getLogin(), login, fileDirectory, client);
        sendFile.start();
    }
}
