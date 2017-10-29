package com.paralysis;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.UUID;

public class ResultPane extends Application {
    private static final int CELL_HEIGHT = 70;
    private static String searchWord;
    private static String extension;

    private ListView<JSONObject> list;
    private ChromeDriver driver;
    private WebsocketClientEndpoint clientEndPoint;

    public void start(Stage primaryStage) throws URISyntaxException, IOException {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Alan\\Desktop\\nctu-hack\\java-popup\\chromedriver.exe");

        UUID uuid = UUID.randomUUID();
        String processName = "java.exe";


        clientEndPoint = new WebsocketClientEndpoint(new URI("ws://127.0.0.1:10134"));
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
        clientEndPoint.setMessageHandler(mHandler);
        clientEndPoint.sendMessage(setup.toString());

        OkHttpClient client = new OkHttpClient();

        // Create request for remote resource.
        Request request = new Request.Builder()
                .url(String.format("https://api.stackexchange.com/2.2/search?order=asc&sort=relevance&tagged=javascript&intitle=%s&site=stackoverflow", searchWord))
                .build();

        JSONObject items[] = new JSONObject[9];
        // Execute the request and retrieve the response.
        try {
            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();
            JSONArray arr = new JSONObject(body.string()).getJSONArray("items");
            for(int i = 0; i < 9 && i < arr.length(); i++) {
                JSONObject post = arr.getJSONObject(i);
                if(post.getInt("answer_count") == 0) {
                    i--;
                    continue;
                }
                post.put("type", "search_result");
                items[i] = post;
            }
            items[8] = new JSONObject("{'type': 'exit'}");
        } catch (IOException|JSONException e) {
            e.printStackTrace();
        }

        list = new ListView<>();
        list.setCellFactory(lst -> new MyListCell());
        list.setItems(FXCollections.observableArrayList(items));
        list.setPrefSize(600, 9 * CELL_HEIGHT + 80);
        if(list.getItems().size() != 0) {
            list.getSelectionModel().select(0);
            list.getFocusModel().focus(0);
        }
        list.setOnMouseClicked(e -> pressItem());
        Scene scene = new Scene(list);
        scene.getStylesheets().add(getClass().getResource("/list.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }

    private class MyListCell extends ListCell<JSONObject> {

        private VBox box;
        private Label title;
        private Label keyWord;
        private HBox icons;
        private Label see;
        private Label check;

        private HBox exitBox;

        public MyListCell() {
            title = new Label();
            title.setFont(new Font(22));
            title.setTextFill(Paint.valueOf("#E0E5EB"));
            keyWord = new Label();
            keyWord.setTextFill(Paint.valueOf("#E0E5EB"));
            icons = new HBox(15);
            see = new Label();
            see.setTextFill(Paint.valueOf("#E0E5EB"));
            ImageView seeView = new ImageView(new Image(this.getClass().getResource("/eye.png").toExternalForm()));
            TextFlow seePack = new TextFlow(see, new Text(" "), seeView);
            check = new Label();
            check.setTextFill(Paint.valueOf("#E0E5EB"));
            ImageView checkView = new ImageView(new Image(this.getClass().getResource("/checked.png").toExternalForm()));
            TextFlow checkPack = new TextFlow(check, new Text(" "), checkView);
            icons.getChildren().setAll(seePack, checkPack);
            icons.setAlignment(Pos.CENTER_RIGHT);
            box = new VBox();
            box.getChildren().setAll(title, keyWord, icons);
            box.setAlignment(Pos.CENTER_LEFT);
            box.setPrefHeight(CELL_HEIGHT);

            ImageView logoutView = new ImageView(new Image(this.getClass().getResource("/exit.png").toExternalForm()));
            Label exitText = new Label("Exit ");
            exitText.setFont(new Font(30));
            exitText.setTextFill(Paint.valueOf("#E0E5EB"));
            TextFlow exitFlow = new TextFlow(exitText, logoutView);
            exitBox = new HBox();
            exitBox.getChildren().setAll(exitFlow);
            exitBox.setAlignment(Pos.CENTER);

            prefWidthProperty().bind(list.widthProperty().subtract(2));
            setMaxWidth(Control.USE_PREF_SIZE);
        }

        @Override
        public void updateItem(JSONObject item, boolean empty) {
            super.updateItem(item, empty);
            try {
                if (item != null) {
                    if (item.getString("type").equals("exit")) {
                        setGraphic(exitBox);
                    } else {
                        title.setText(Jsoup.parse(item.getString("title")).text());
                        JSONArray tagArray = item.getJSONArray("tags");
                        StringBuilder tagStr = new StringBuilder();
                        for(int i = 0; i < tagArray.length(); i++) {
                            if(i != 0)
                                tagStr.append(", ");
                            tagStr.append(tagArray.getString(i));
                        }
                        keyWord.setText(String.join(",", tagStr));
                        see.setText(item.getString("view_count"));
                        check.setText(item.getString("answer_count"));
                        box.getChildren().setAll(title, keyWord, icons);
                        setGraphic(box);
                    }
                } else
                    setGraphic(null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private WebsocketClientEndpoint.MessageHandler mHandler = new WebsocketClientEndpoint.MessageHandler() {
        @Override
        public void onTouch() {
            System.out.println("onTouch");
            pressItem();
        }

        @Override
        public void onScroll(int tick) {
            System.out.println("onScroll: " + tick);
            int size = list.getItems().size();
            int idx = (list.getSelectionModel().getSelectedIndex() - tick + size) % size;
            Platform.runLater(() -> list.getSelectionModel().select(idx));
        }
    };

    private void pressItem() {
        JSONObject item = list.getSelectionModel().getSelectedItem();
        try {
            if (item.getString("type").equals("exit")) {
                clientEndPoint.close();
                if(driver != null)
                    driver.quit();
                System.exit(0);
            } else {
                System.out.println("Go to stack overflow");
                if(driver != null)
                    driver.quit();
                driver = new ChromeDriver();
                driver.get(item.getString("link"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        searchWord = args[0];
        String toks[] = args[1].split("[.]");
        System.out.print(Arrays.toString(toks));
        extension = toks[toks.length - 1];
        launch(args);
    }

    public static long getPID() {
        String processName =
                java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        return Long.parseLong(processName.split("@")[0]);
    }
}
//.list-view .list-cell:even {
//        -fx-background-color: #252526;
//        -fx-text-fill: #E0E5EB;
//        }
//        .list-view .list-cell:odd {
//        -fx-background-color: #252526;
//        -fx-text-fill: #E0E5EB;
//        }
//
//        .list-cell:filled:selected:focused, .list-cell:filled:selected {
//        -fx-background-color: #ffffff;
//        -fx-text-fill: #ffffff;
//        }