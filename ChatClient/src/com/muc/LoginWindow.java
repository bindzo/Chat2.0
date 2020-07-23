package com.muc;


import com.vdurmont.emoji.EmojiParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LoginWindow extends JFrame{
    private final ChatClient client;
    JTextField loginField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JButton loginButton = new JButton("Login");
    JButton registerButton = new JButton("Register");


    public LoginWindow(){
        super("Login");

        this.client = new ChatClient("localhost",8818);
        client.connect();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.add(loginField);
        p.add(passwordField);
        p.add(loginButton);
        p.add(registerButton);


        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doRegister();
            }
        });
        getContentPane().add(p, BorderLayout.CENTER);

        pack();

        setVisible(true);
    }

    private void doRegister() {
        String login = loginField.getText();
        String password =passwordField.getText();
        try {
            if(client.register(login,password)){
                loginField.setText("");
                passwordField.setText("");
                JOptionPane.showMessageDialog(this,"Register successful");

            }else{
                //show error message
                JOptionPane.showMessageDialog(this,"Login have been used. Please try another one!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doLogin() {
        String login = loginField.getText();
        String password =passwordField.getText();

        try {
            if(client.login(login,password)){
                UserListPane userListPane = new UserListPane(client);
                userListPane.initComponents();

                setVisible(false);

            }else{
                //show error message

                JOptionPane.showMessageDialog(this,"Invalid login/password.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        LoginWindow loginWin = new LoginWindow();
        loginWin.setVisible(true);

    }
}
