package com.cloud.util;

import com.cloud.coder.ActionsEnum;
import com.cloud.coder.SocketDTO;
import com.cloud.coder.javadoc.dbinfo.Document;
import com.cloud.coder.javadoc.info.ClassApi;
import com.cloud.coder.javadoc.info.MethodApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.java_websocket.WebSocket;

import javax.swing.tree.DefaultMutableTreeNode;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/6/3 17:07
 */
public class WsClientUtil {

    private static NettyWsClient nettyWsClient;
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    static {
        try {
            nettyWsClient = new NettyWsClient("ws://127.0.0.1:10000");
            WsClientUtil.getClient();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static NettyWsClient getClient(){
        if (!nettyWsClient.getReadyState().equals(WebSocket.READYSTATE.OPEN) && !nettyWsClient.getReadyState().equals(WebSocket.READYSTATE.CONNECTING)){
            nettyWsClient.connect();
            System.out.println("正在連接 ...");
            while (!nettyWsClient.getReadyState().equals(WebSocket.READYSTATE.OPEN)){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return nettyWsClient;
    }

    public static void apiIsExisted(MethodApi methodApi, DefaultMutableTreeNode node){
        executorService.execute(new Runnable() {
            @Override
            public void run() {

                SocketDTO socketDTO = new SocketDTO();
                socketDTO.setAction(ActionsEnum.InsertCategory);
                socketDTO.setData(methodApi);
                String jsonString = new Gson().toJson(socketDTO);

                WsClientUtil.getClient().send(jsonString);
            }
        });
    }

    public static void insertDocument(String qualifiedName, String method){
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Document document = new Document();
                String s = new Gson().toJson(document);
                WsClientUtil.getClient().send(s);
            }
        });
    }
}
