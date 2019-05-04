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

            InputStream socketIns = client.getInputStream();  //用户先输入，让client读取/识别  客户端的Socket对象上的getInputStream方法得到输入流其实就是从服务器端发回的数据。
            OutputStream socketOut = client.getOutputStream();//获取发送数据的流   客户端的Socket对象上的getOutputStream方法得到的输出流其实就是发送给服务器端的数据。

            // 开启新的一条线程接收server发送过来的数据
            ClientChatHandler clientChatHandler = new ClientChatHandler(socketIns);
            new Thread(clientChatHandler).start();

            //创建Scanner类的实例化对象  用于读取客户端用户键盘输入的数据 （使用Scanner类实现数据输入）
            Scanner scanner = new Scanner(System.in);

            boolean chatIsNotOver = true;       //判断聊天没结束的值是true
            while (chatIsNotOver) {
                while (!clientChatHandler.getHasAnswer()) {
                    synchronized (clientChatHandler) {
                        clientChatHandler.wait();
                    }
                }

                System.out.print("我：");
                // client发送第一个问话
                String askStr = scanner.nextLine();  //使用Scanner类中的nextLine()可以扫描到一行内容并实现字符串String的获取。

                if (askStr.equals("#endChat#")) {//用户想结束会话
                    askStr = "#END#";   //发给服务器     重新再赋值
                    socketOut.write(askStr.getBytes());   //String的getBytes()方法是得到一个操作系统默认的编码格式的字节数组。
                    // 提示子线程：聊天已经结束了
                    clientChatHandler.setOver(true);

                    break;//跳出循环
                }

                // 其余的聊天和请求文件 都是直接发送过去就行了！
                socketOut.write(askStr.getBytes());  //String的getBytes()方法是得到一个操作系统默认的编码格式的字节数组。

                // 设置当前聊天内容尚未 回复
                synchronized (clientChatHandler) {
                    clientChatHandler.setHasAnswer(false);  //false 未 回复
                }
            }
        } catch (IOException e) {     //输入或输出异常
            e.printStackTrace();
        } catch (InterruptedException e) {    //中断异常
            e.printStackTrace();
        } catch (Exception e) {    //wait()
            e.printStackTrace();
        } finally {
            if (client != null) {  //null表示没有指向任何字符
                    try {
                        client.close();   //释放资源
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
    }
}
//开启的新线程是通过实现Runnable接口创建的，重写的run()方法中实现了客户端  只做接收数据 ，并将接收的数据打印出来。
class ClientChatHandler implements Runnable {
    private InputStream socketIns;
    private boolean isOver = false;//初始化 标识socket是否已经结束  已结束
    private volatile boolean hasAnswer = true;// 标识当前ask是否已经 有回复

    public ClientChatHandler(InputStream inputStream) {
        this.socketIns = inputStream;
    }

    public void setOver(boolean over) {
        isOver = over;
    }

    public void setHasAnswer(boolean hasAnswer) {   //初始化 设置是否有回复
        this.hasAnswer = hasAnswer;
    }
    public boolean getHasAnswer() {   //获取回复
        return hasAnswer;
    }

    public void run() {
        while (!isOver) {// 一直循环，直到收到服务器端的通知
            try {
                // 一次读入当前接收到的所有数据       （来自服务器的数据）
                byte[] bytes = new byte[1024 * 1024 * 10];   //接收数据的最大限度
                int getDataLength = socketIns.read(bytes);      //实际接收到的数据长度

                byte[] chatContent = new byte[getDataLength];   // 把实际接收到的数据 长度 再读进容器定义，只作为长度，不包括内容
                for (int i = 0; i < getDataLength; i++) {
                    chatContent[i] = bytes[i];          //将实际接收到的数据内容平移装进容器   字节数组
                }
                System.out.println("server：" + new String(chatContent));  //将字节数组转换为字符串

                // 聊天已收到回复
                synchronized (this) {   //this指向当前调用它的对象
                    this.setHasAnswer(true);
                    this.notify();   //notify()的调用者是同步锁对象
                }
            } catch (Exception e) {
                System.out.println("连接已经断开...");
            }
        }
    }
}
