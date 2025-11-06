package org.example.Server;
import socketserver.MessageBroadcaster;
import socketserver.SocketServer;

public class AppServer {
    public static SocketServer requestServer;
    public static MessageBroadcaster messageBroadcaster;

    public static void initialize(int requestPort, int messagePort) {
    requestServer =new
    SocketServer(requestPort);
    messageBroadcaster =new
    MessageBroadcaster(messagePort);
    requestServer.setMessageBroadcaster(messageBroadcaster);
    }
    public static SocketServer getRequestServer() {
        return requestServer;
    }
    public static MessageBroadcaster getMessageBroadcaster() {
            return messageBroadcaster;
    }
}
