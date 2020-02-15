package frc.targettracking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import edu.wpi.first.wpilibj.Notifier;
import frc.gen.BIGData;

public class JetsonCamera implements Runnable {
    // thread that contains code to connect to and read from socket
    private Thread thread;
    // socket that is connected to jetson
    private Socket socket;
    // port of the jetson to connect to
    private int port;
    // ip address of the jetson
    private String jetsonAddress;
    // reader that reads from socket
    private BufferedReader stdIn;
    
    // default port of jetson to connect to
    private final static int DEFAULT_PORT = 1337;

    public JetsonCamera() {
        port = BIGData.getInt("jetson_camera_port");
        if (port == -1) {
            System.out.println("unable to read valid config file value for port number for camera on jetson"
                + ", using default port " + DEFAULT_PORT);
            port = DEFAULT_PORT;
        }
        jetsonAddress = BIGData.getString("jetson_address");
        thread = new Thread(this);
        thread.start();
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                if (stdIn == null || socket == null || socket.isClosed() || !socket.isConnected() || !socket.isBound()) {
                    System.out.println("camera code is attempting to connect to jetson at address " + jetsonAddress + ",port=" + port);
                    if (!connect()) {
                        BIGData.putJetsonCameraConnected(false);
                        // if we don't connect, wait before trying to connect again
                        Thread.sleep(500);
                    }
                } else {
                    BIGData.putJetsonCameraConnected(true);
                    cameraData();
                }
            } catch (Exception e) {
                System.out.println("Outer exception caught in CAMERA code. unknown error. camera code still trying to connect to jetson socket");
            }
        }
    }


    public boolean connect() {
        boolean connected = false;
        try {
            socket = new Socket(jetsonAddress, port);
            stdIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connected to jetson address=" + jetsonAddress + " at port=" + port);
            connected = true;
        } catch (UnknownHostException e1) {
            socket = null;
            stdIn = null;
        } catch (IOException e1) {
            socket = null;
            stdIn = null;
        } catch (Exception e) {
            socket = null;
            stdIn = null;
            System.out.println("UNKNOWN ERROR: SOMETHING WENT SERIOUSLY WRONG IN CAMERA CONNECTING!");
        }
        return connected;
    }

    public void cameraData() {
        try {
            String in = stdIn.readLine();
            if (in != null) {
                String[] data = in.replace("(", "").replace(")", "").split(",");
                BIGData.updateCamera(Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("unable to parse lidar data, NullPointerException");
        } catch (NumberFormatException e) {
            System.out.println("unable to parse lidar data, NumberFormatException");
        } catch (Exception e) {
            System.out.println("UNKNOWN ERROR: Attempted to read lidar data but something bad happened.");
        }

    }

}
