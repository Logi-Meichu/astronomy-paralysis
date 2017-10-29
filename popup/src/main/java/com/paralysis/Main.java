package com.paralysis;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.UUID;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws URISyntaxException, IOException {
        UUID uuid = UUID.randomUUID();
        String processName = "java.exe";

        WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI("ws://127.0.0.1:10134"));
        JSONObject setup = new JSONObject();
        try {
            setup.put("message_type", "register");
            setup.put("plugin_guid", uuid);
            setup.put("PID", getPID());
            setup.put("execName", processName);
            setup.put("manifestPath", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        clientEndPoint.sendMessage(setup.toString());

        BorderPane mBorderPane = new BorderPane();
        mBorderPane.setPrefSize(400, 300);
        mBorderPane.setCenter(new Label(uuid.toString() + "\n" + getPID() + "\n" + processName));
        Scene scene = new Scene(mBorderPane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static long getPID() {
        String processName =
                java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        return Long.parseLong(processName.split("@")[0]);
    }
}