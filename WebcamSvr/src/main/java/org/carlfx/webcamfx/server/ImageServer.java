package org.carlfx.webcamfx.server;

import javax.imageio.ImageIO;
import javax.media.*;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.*;

/**
 * User: cdea
 *
 */
public class ImageServer {
    public static int FPS = 1000 / 30; // 30 FPS

    BlockingQueue<SocketWorker> workers = new LinkedBlockingQueue<>();
    ServerSocket serverSocket;
    boolean stopped = false;
    Player player = null;

    /**
     * This will start the server and listen to clients.
     */
    public void start() {
        Vector v = CaptureDeviceManager.getDeviceList(null);
        CaptureDeviceInfo cam = null;
        for (int i=0; i<v.size(); i++) {
            CaptureDeviceInfo dev = (CaptureDeviceInfo) v.elementAt(i);
            System.out.println(dev.getName());
            if (dev.getName().startsWith("vfw")) {
                cam = dev;
//                break;
            }
        }
//        if (cam == null) {
//            System.out.println("no cam");
//            System.exit(0);
//        }
        MediaLocator locator = cam.getLocator();
        FrameGrabbingControl fgc = null;
        try {
            player = Manager.createRealizedPlayer(locator);
            fgc = (FrameGrabbingControl) player.getControl("javax.media.control.FrameGrabbingControl");
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        final FrameGrabbingControl fgc2 = fgc;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!stopped) {
                    if (workers.size() == 0) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    Buffer buf = fgc2.grabFrame();
                    BufferToImage buffImg = new BufferToImage((VideoFormat)buf.getFormat());
                    BufferedImage image = (BufferedImage) buffImg.createImage(buf);
                    byte[] imageData = null;
                    try {
                        imageData  = serializeImage(image, "jpeg");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    for (SocketWorker w:workers) {
                       w.send(imageData);
                    }

                }
            }
        }).start();
        run();
    }

    void run()  {
        ExecutorService exeSvc = Executors.newFixedThreadPool(20);
        try{

            serverSocket = new ServerSocket(4444, 10);
            int counter = 0;
            while(true) {
                //wait for client connection
                System.out.println("Waiting for connection");
                Socket connection = serverSocket.accept();
                System.out.println("Connection received from " + connection.getInetAddress().getHostName());

                SocketWorker sw = new SocketWorker(counter++, connection, this);
                exeSvc.execute(sw);

                workers.put(sw);
            }
        } catch(IOException ioException){
            ioException.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                exeSvc.shutdown();
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static byte[] serializeImage(BufferedImage image, String fileType) throws IOException {
        if (image == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, fileType, out);
        out.flush();
        return out.toByteArray();
    }
}

class SocketWorker implements Runnable{
    int FPS = ImageServer.FPS;
    ObjectOutputStream out;
    ObjectInputStream in;
    private Socket socket;
    private ImageServer ims;
    private int counter;
    private ExecutorService executorService;
    BlockingQueue<byte[]> IMAGES = new ArrayBlockingQueue<>(100);

    public SocketWorker(int counter, Socket socket, ImageServer ims) {
        this.counter = counter;
        this.socket = socket;
        this.ims = ims;
        this.executorService = executorService;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {

        while (true) {
            try {
                byte[] imageData = IMAGES.take();

                if (imageData != null) {
                    out.writeObject(imageData);
                    out.reset();
                    out.flush();
                    System.out.println("Server sent client #" + counter );
                }
                Thread.sleep(FPS);
            } catch (Exception e) {
                close();
            }
        }

    }

    public void close() {
        ims.workers.remove(this);
        close(in);
        close(out);
        close(socket);
        System.out.println("Client #" + counter + " is disconnected.");
    }
    private void close(AutoCloseable stream) {
        try{
            stream.close();
        } catch(Exception e){
            // don't show the close attempt
        }
    }
    public void send(byte[] image) {
        try{
            if (image != null) {
                IMAGES.put(image);
                //System.out.println("Server sent client #" + counter );
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}