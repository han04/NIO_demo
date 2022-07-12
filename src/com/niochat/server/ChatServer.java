package com.niochat.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

//server
public class ChatServer {

    //启动方法
    public void startServer() throws IOException {
        //1.创建selector
        Selector selector = Selector.open();
        //2.创建serverSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        //3.为channel绑定监听端口
        serverSocketChannel.bind(new InetSocketAddress(8001));
            //设置非阻塞
        serverSocketChannel.configureBlocking(false);

        //4.将channel注册到selector
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器成功启动了");
        //5.等待新连接接入
        for(;;){
            //获取channel数量
            int readChannel = selector.select();
            if(readChannel == 0){
                continue;
            }
            //获取可用集合
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while(iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();

                //移除set集合当前selectorKey
                iterator.remove();
                //6.根据就绪状态，调用对应方法
                //accept
                if(selectionKey.isAcceptable()){
                   acceptOperator(serverSocketChannel,selector);
                }
                //可读状态
                if(selectionKey.isReadable()){
                    readOperator(selector,selectionKey);
                }
            }
        }


    }
    //处理可读状态
    private void readOperator(Selector selector,SelectionKey selectionKey) throws IOException {
        //1.从SelectionKey获取就绪通道
        SocketChannel socketChannel = (SocketChannel)selectionKey.channel();
        //2.创建Buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        //3.循环读取客户端消息
        int readLength  = socketChannel.read(byteBuffer);
        String message ="";
        if(readLength > 0){
            //切换模式
            byteBuffer.flip();
            //读取内容
            message += StandardCharsets.UTF_8.decode(byteBuffer);
        }
        //4.channel再次注册到selector  监听可读状态
        socketChannel.register(selector,SelectionKey.OP_READ);
        //5.广播
        if(message.length() > 0){
            System.out.println(message);
            castOtherClient(message,selector,socketChannel);
        }
    }

    private void castOtherClient(String message, Selector selector, SocketChannel socketChannel) throws IOException {
        //1.获取所有已经接入的客户端
        Set<SelectionKey> selectionKeySet = selector.selectedKeys();

        //2.循环所有的channel广播消息
        for(SelectionKey selectionKey : selectionKeySet){
            Channel tarChannel = selectionKey.channel();
            //不给自己发送
            if(tarChannel instanceof SocketChannel && tarChannel != socketChannel){
                ((SocketChannel) tarChannel).write(StandardCharsets.UTF_8.encode(message));
            }
        }
    }


    //处理接入状态
    private void acceptOperator(ServerSocketChannel serverSocketChannel,Selector selector) throws IOException {
        //1.创建socketChannel
        SocketChannel socketChannel = serverSocketChannel.accept();
        //2.把socketChannel设置非阻塞模式
        socketChannel.configureBlocking(false);

        //3.把channel注册到selector 监听可读状态
        socketChannel.register(selector,SelectionKey.OP_READ);

        //4.客户端回复提示
        socketChannel.write(StandardCharsets.UTF_8.encode("成功进入聊天室，打招呼吧"));
    }

    public static void main(String[] args) {
        try {
            new ChatServer().startServer();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
