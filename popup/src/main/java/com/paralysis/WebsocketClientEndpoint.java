package com.paralysis;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 * ChatServer Client
 *
 * @author Jiji_Sasidharan
 */
@ClientEndpoint
public class WebsocketClientEndpoint {
    private boolean preTouchEvent = false;
    private int preTouchState = -1;
    private Session userSession = null;
    private MessageHandler messageHandler;

    public WebsocketClientEndpoint(URI endpointURI) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        System.out.println("opening websocket");
        this.userSession = userSession;
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        System.out.println("closing websocket");
        this.userSession = null;
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            if(json.getString("message_type").equals("crown_touch_event")) {
                if(json.getInt("touch_state") == 0 && preTouchEvent && preTouchState == 1)
                    messageHandler.onTouch();
                preTouchEvent = true;
                preTouchState = json.getInt("touch_state");
            } else {
                preTouchEvent = false;
                preTouchState = -1;
                if(json.getString("message_type").equals("crown_turn_event"))
                    messageHandler.onScroll(json.getInt("ratchet_delta"));
            }
        } catch (JSONException e) {
            preTouchEvent = false;
            preTouchState = -1;
            e.printStackTrace();
        }
    }

    /**
     * register message handler
     *
     * @param msgHandler
     */
    public void setMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    /**
     * Send a message.
     *
     * @param message
     */
    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }

    public void close() {
        try {
            if(userSession != null)
                userSession.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Message handler.
     *
     * @author Jiji_Sasidharan
     */
    public interface MessageHandler {
        void onTouch();
        void onScroll(int tick);
    }
}