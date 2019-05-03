package com.test.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatRobot {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(8080));
            while (true){
                final Socket socket = serverSocket.accept();
                new Thread(new ChatRobotHandler(socket)).start();
            }

        } catch (IOException e) {
            System.out.println("抛异常了...");

        }
        System.out.println("");

    }
}

class ChatRobotHandler implements Runnable {
    Socket socket ;


    public ChatRobotHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = this.socket.getInputStream();
            byte[] readBytes = new byte[1024];

            while(inputStream.read(readBytes) != -1){

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}