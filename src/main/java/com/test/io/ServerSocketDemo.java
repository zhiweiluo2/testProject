package com.test.io;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServerSocketDemo {
    public static void main(String[] args) throws IOException {
        //	1、创建服务器 ServerSocket对象   监听指定端口
        ServerSocket serverSocket  =  new ServerSocket(1025);
        System.out.println("服务器成功启动...");

        //  2、使用while循环不停的接收客户端发送的请求
        while(true){
            //  3、调用ServerSocket对象accept（）方法接收客户端的请求，得到Socket对象
            Socket socket =  serverSocket.accept();
            System.out.println("有client连接进入...");
            // 4. 分配线程处理会话
            new Thread(new ServerChatHandler(socket)).start();
        }
    }
}

class ServerChatHandler implements Runnable {

    private Socket socket;

    public ServerChatHandler(Socket socket){
        this.socket = socket;
    }

    public void run() {
        OutputStream outputStream  = null;
        InputStream inputStream = null;
        //创建Scanner对象
        Scanner sc = new Scanner(System.in);//System.in表示标准化输出，也就是键盘输出
        try {
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();

            byte[] content = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(content)) != -1) {
                byte[] tempBytes = new byte[len];
                for (int i = 0; i < len; i++) {
                    tempBytes[i] = content[i];
                }

                String str = new String(tempBytes);
                System.out.println("client：" + str);

                // 如果"#end#"， 意味着client想终止会话。
                if ("#END#".equals(str)) {
                    socket.close();//关闭连接
                    System.out.println("结束与客户端交互数据");
                    return;//退出函数, 则线程结束
                }

                System.out.print("我：");
                //利用hasNextXXX()判断是否还有下一输入项
                String answerStr = sc.nextLine();
                outputStream.write(answerStr.getBytes());
                outputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}



