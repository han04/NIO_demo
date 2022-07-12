package com.niochat.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class ClientThread implements Runnable{
    private Selector selector;
    public ClientThread(Selector selector){
        this.selector = selector;
    }
    @Override
    public void run() {
        try{
            //5.循环 等待新连接接入
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

                    //可读状态
                    if(selectionKey.isReadable()){
                        readOperator(selector,selectionKey);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private void readOperator(Selector selector,SelectionKey selectionKeyS) throws IOException {
        //1.从SelectionKey获取就绪通道
        SocketChannel socketChannel = (SocketChannel)selectionKeyS.channel();
        //2.创建Buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        //3.循环读取客户端消息
        int readLength  = socketChannel.read(byteBuffer);
        String message ="";
        if(readLength > 0){
            //切换模式
            byteBuffer.flip();
            //读取内容
            message += Charset.forName("UTF-8").decode(byteBuffer);
        }
        //4.channel再次注册到selector  监听可读状态
        socketChannel.register(selector,SelectionKey.OP_READ);
        //5.广播
        if(message.length() > 0){
            System.out.println(message);
        }
    }
}
