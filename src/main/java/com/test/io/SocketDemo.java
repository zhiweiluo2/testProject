package com.test.io;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class SocketDemo {
    public static void main(String[] args) {
        Socket client = null;
        try {
            //1、创建一个Socket对象并连接到给出地址和端口号的计算机
            client = new Socket("localhost", 1025);
            System.out.println("成功连接server...");

            InputStream socketIns = client.getInputStream();  //得到接收数据的流
            OutputStream socketOut = client.getOutputStream();//获取发送数据的流

            // 开启新的一条线程接收server发送过来的数据
            ClientChatHandler clientChatHandler = new ClientChatHandler(socketIns);
            new Thread(clientChatHandler).start();

            Scanner scanner = new Scanner(System.in);//读取键盘输入
            boolean chatIsNotOver = true;
            while (chatIsNotOver) {
                while (!clientChatHandler.getHasAnswer()){
                    synchronized (clientChatHandler){
                        clientChatHandler.wait();
                    }
                }

                System.out.print("我：");
                // client发送第一个问话
                String askStr = scanner.nextLine();

                if (askStr.equals("#endChat#")) {//用户想结束会话
                    askStr = "#END#";   //发给服务器
                    socketOut.write(askStr.getBytes());
                  // 提示子线程：聊天已经结束了
                    clientChatHandler.setOver(true);

                    break;//跳出循环
                }

                // 其余的聊天和请求文件 都是直接发送过去就行了！
                socketOut.write(askStr.getBytes());

                // 设置当前聊天内容尚未回复
                synchronized (clientChatHandler){
                    clientChatHandler.setHasAnswer(false);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        } finally{
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

class ClientChatHandler implements Runnable {
    private InputStream socketIns;
    private boolean isOver = false;//初始化 标识socket是否已经结束
    private volatile boolean hasAnswer = true;// 标识当前ask是否已经有回复

    public ClientChatHandler(InputStream inputStream) {
        this.socketIns = inputStream;
    }

    public void setOver(boolean over) {   //
        isOver = over;
    }

    public void setHasAnswer(boolean hasAnswer){
        this.hasAnswer = hasAnswer;
    }

    public boolean getHasAnswer(){
        return hasAnswer;
    }

    @Override
    public void run() {
        while (!isOver) {// 一直循环，直到收到通知
            try {
                // 一次读入当前接收到的所有数据
                byte[] bytes = new byte[1024*1024*10];
                int getDataLength = socketIns.read(bytes);

                // 判断是否是接收文件
                // 1. 提取标识头
                int markLen = "#SEND_FILE#".length(); //
                byte[] tempBytes = new byte[markLen];
                for (int i = 0; i < markLen; i++) {
                    tempBytes[i] = bytes[i];
                }
                String tempStr = new String(tempBytes);
                // 2. 判断
                if ("#SEND_FILE#".equals(tempStr)) {// true : 接收文件
                    //获取文件名字
                    int fileNameLen = markLen;
                    for (; fileNameLen < getDataLength; fileNameLen++) {
                        if ('#' == bytes[fileNameLen]){
                            break;
                        }
                    }
                    fileNameLen -= markLen;
                    byte[] fileNameBytes = new byte[fileNameLen];
                    for (int j = 0 ; j < fileNameLen; j++){
                        fileNameBytes[j] = bytes[markLen + j];
                    }
                    String fileName = new String(fileNameBytes);

                    // 将接收到的字节存入磁盘中
                    String filePath = "D:\\java\\test\\" + fileName;
                    File aFile = new File(filePath);
                    aFile.createNewFile();

                    FileOutputStream fileOutputStream = new FileOutputStream(aFile);
                    fileOutputStream.write(bytes, markLen+fileNameLen+1, getDataLength - (markLen+fileNameLen)-1);
                    fileOutputStream.flush();

                    System.out.println("文件["+filePath+"]下载完毕");
                } else {
                    // 剩下的一种情况就是聊天
                    byte[] chatContent = new byte[getDataLength];
                    for (int i = 0; i < getDataLength; i++) {
                        chatContent[i] = bytes[i];
                    }

                    System.out.println("server：" + new String(chatContent));
                }


                // 聊天已收到回复
                synchronized (this){
                    this.setHasAnswer(true);
                    this.notify();
                }
            } catch (Exception e) {
                System.out.println("连接已经断开...");
            }
        }
    }
}
