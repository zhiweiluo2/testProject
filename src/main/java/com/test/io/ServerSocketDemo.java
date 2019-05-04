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
            //  3、调用ServerSocket对象accept（）方法接收客户端发送的请求，得到Socket对象
            Socket socket =  serverSocket.accept();
            System.out.println("有client连接进入...");
            // 4. 分配线程处理会话   服务器端为每个来访问的客户端创建一个对应的Socket，并且开启一个新的线程使两个Socket建立专线进行通信。
            new Thread(new ServerChatHandler(socket)).start();  // ServerChatHandler 服务器聊天处理器
        }
    }
}
//开启的新线程是通过实现Runnable接口创建的，重写的run()方法中实现了服务器端接收、发送数据的功能。
class ServerChatHandler implements Runnable {    //聊天处理线程

    private Socket socket;          //持有一个Socket类型的属性

    public ServerChatHandler(Socket socket){    //构造方法中把Socket对象作为实参传入
        this.socket = socket;
    }

    public void run() {
        OutputStream outputStream  = null;
        InputStream inputStream = null;
        //创建Scanner类的实例化对象 （使用Scanner类实现数据输入）
        Scanner sc = new Scanner(System.in);//用于读取键盘输入的数据   //System.in表示标准化输入
        try {
            outputStream = socket.getOutputStream();   //服务端的Socket对象上的getOutputStream方法得到的输出流其实就是发送给客户端的数据。
            inputStream = socket.getInputStream();   // 服务端的Socket对象上的getInputStream方法得到的输入流其实就是从客户端发送给服务器端的数据流。

            byte[] content = new byte[1024];   //定义一个字节数组  接收客户端发来的数据  容量大小可调节
            int len = 0;        //定义一个int类型的变量len ,初始值为0
            while ((len = inputStream.read(content)) != -1) {  //读取已装进content字节数组中的数据，并赋值给len
                byte[] tempBytes = new byte[len];     // 把实际接收到的数据"长度 "再读进容器定义，只作为长度，不包括内容
                for (int i = 0; i < len; i++) {
                    tempBytes[i] = content[i];   //将实际接收到的数据内容 平移 装进容器   字节数组
                }

                String str = new String(tempBytes);  //将字节数组转换为字符串  解码
                System.out.println("client：" + str);   //将实际接收到的数据（客户端发来的数据） 以字符串形式输出

                // 如果"#end#"， 意味着client想终止会话。
                if ("#END#".equals(str)) {    //比较字符串与“”的值是否相等
                    socket.close();//关闭连接
                    System.out.println("结束与客户端交互数据");
                    return;//退出函数, 则线程结束
                }

                System.out.print("我：");
                //利用hasNextXXX()判断是否还有下一输入项
                String answerStr = sc.nextLine();  //使用Scanner类中的nextLine()可以扫描到一行内容并实现字符串String的获取。
                outputStream.write(answerStr.getBytes());  //String的getBytes()方法是得到一个操作系统默认的编码格式的字节数组。
                outputStream.flush();       //flush() 方法刷新此输出流并强制将所有缓冲的输出字节被写出
            }
        } catch (IOException e) {    //输入或输出异常
            e.printStackTrace();

        } finally {
            try {
                socket.close();
            } catch (IOException e) {    //输入或输出异常
                e.printStackTrace();
            }
        }

    }
}



