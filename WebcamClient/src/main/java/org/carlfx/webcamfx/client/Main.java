/*
* Copyright (c) 2013. Carl Dea
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.carlfx.webcamfx.client;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A simple JavaFX client using sockets to communicate with the server
 * to queue up jpeg images
 */
public class Main extends Application{
    BlockingQueue<byte[]> IMAGES = new LinkedBlockingQueue<>();
    public static int FPS = 1000 / 30; // 60 FPS
    Socket requestSocket;
    ObjectOutputStream out;
    ObjectInputStream in;
    protected static boolean stop = false;
    int frameCnt = 0;
    long starttimeNano = 0;
    long lastFPSTime = 0;

    void run() {
        try {
            //1. creating a socket to connect to the server
            requestSocket = new Socket("localhost", 4444);
            System.out.println("Connected to localhost in port 4444");
            //2. get Input and Output streams
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            //out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());
            //3. Communicating with the server

            while(true) {
                try{
                    //Object rawObj = null;
                    byte[] rawObj = null;
                    if (in != null) {

                        rawObj = (byte[])in.readObject();
                    }

                    if (rawObj != null && rawObj.length > 0){
                        IMAGES.put(rawObj);
                    }
                    Thread.sleep(FPS);
                } catch (Exception e) {
                    System.out.println("huh?");
                    e.printStackTrace();
                    break;
                }
            }

        }
        catch(UnknownHostException unknownHost){
            System.err.println("You are trying to connect to an unknown host!");
        } catch(IOException ioException){
            ioException.printStackTrace();
        } finally{
            // Closing connection
            System.out.println("closing stuff");
            close(requestSocket);
            close(in);
            close(out);
            in = null;
            out = null;
            requestSocket = null;
        }
    }

    private void close(AutoCloseable stream) {
        try{
            stream.close();
        } catch(Exception e){
            // don't show the close attempt
        }
    }
    public static void main(String args[])
    {
        Application.launch(args);

    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("client fx image consumer");
        Group root = new Group();

        Scene scene = new Scene(root, 400, 400, Color.BLACK);
        final ImageView imageView = new ImageView();
        root.getChildren().add(imageView);

        Text text = buildText2("");

        root.getChildren().add(text);

        stage.setScene(scene);


        starttimeNano = System.nanoTime();
        AnimationTimer animationTimer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                frameCnt++;
                long currNano = System.nanoTime();
                try {
                    byte[] imageBytes = IMAGES.take();
                    if (imageBytes != null && imageBytes.length > 0) {
                        Image fxImage = deserializeImage(imageBytes);
                        imageView.setImage(fxImage);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (currNano > lastFPSTime + (1000000000))   // hit one second
                {
                    text.setText("FPS "+ frameCnt);
                    System.out.println(" FPS " + frameCnt);
                    frameCnt = 0;
                    lastFPSTime = currNano;
                }
            }
        };

        animationTimer.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Main.this.run();
                try {
                    Main.this.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        stage.show();

    }

    private Text buildText2(String str) {

        Text t = new Text();
        t.setText(str);
        t.setFont(Font.font(null, FontWeight.BOLD, 20));
        t.setStrokeWidth(1);
        t.setStroke(Color.WHITE);
        t.setFill(Color.RED);
        t.setX(10);
        t.setY(20);
        return t;
    }
    public static Image deserializeImage(byte[] imageData) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(imageData);
        return new Image(in);
    }

    public void stop() throws java.lang.Exception {

        System.exit(0);
        Platform.exit();

    }
}
