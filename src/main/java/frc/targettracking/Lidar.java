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

public class Lidar implements Runnable {
    private Thread thread;
    private Socket socket;
    private int port;
    private String jetsonAddress;
    private BufferedReader stdIn;

    public void run() {
        while (true) {
            try {
                if (stdIn == null || socket == null || socket.isClosed() || !socket.isConnected() || !socket.isBound()) {
                    connect();
                } else {
                    lidarData();
                }
            } catch (Exception e) {
                System.out.println("Outer exception caught in LIDAR code. unknown error");
            }
        }
    }

    public Lidar() {
        port = 1030;
        jetsonAddress = "10.1.92.14";
        thread = new Thread(this);
        thread.start();
    }

    public void connect() {
        System.out.println("start connect pls");
        try {
            socket = new Socket(jetsonAddress, port);
            stdIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connected to jetson address " + jetsonAddress + " at port " + port);
        } catch (UnknownHostException e1) {
            socket = null;
            stdIn = null;
            e1.printStackTrace();
        } catch (IOException e1) {
            socket = null;
            stdIn = null;
            e1.printStackTrace();
        } catch (Exception e) {
            socket = null;
            stdIn = null;
            e.printStackTrace();
            System.out.println("UNKNOWN ERROR: SOMETHING WENT SERIOUSLY WRONG IN LIDAR CONNECTING!");
        }
    }

    public void lidarData() {
        try {
            String in = stdIn.readLine();
            if (in != null) {
                String[] data = in.replace("(", "").replace(")", "").split(",");
                // data[0], data[3] should be in radians.
                BIGData.updateLidar(Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("unable to parse lidar data, NullPointerException");
        } catch (NumberFormatException e) {
            System.out.println("unable to parse lidar data, NumberFormatException");
        } catch (Exception e) {
            System.out.println("UNKNOWN ERROR: Attempted to read lidar data but something bad happened.")
        }

    }

}