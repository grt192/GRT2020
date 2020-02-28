package frc.targettracking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import frc.gen.BIGData;

public class Lidar implements Runnable {
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
    private final static int DEFAULT_PORT = 1030;

    public Lidar() {
        port = BIGData.getInt("jetson_lidar_port");
        if (port == -1) {
            System.out.println("unable to read valid config file value for port number for lidar on jetson"
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
                if (Thread.interrupted()) {
                    return;
                }
                if (stdIn == null || socket == null || socket.isClosed() || !socket.isConnected()
                        || !socket.isBound()) {
                    if (!connect()) {
                        BIGData.put("lidar_connected", false);
                        // if we don't connect, wait before trying to connect again
                        Thread.sleep(500);
                    }
                } else {
                    BIGData.put("lidar_connected", true);
                    lidarData();
                }
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                System.out.println("Outer exception caught in LIDAR code. unknown error");
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
            System.out.println("UNKNOWN ERROR: SOMETHING WENT SERIOUSLY WRONG IN LIDAR CONNECTING!");
        }
        return connected;
    }

    public void lidarData() {
        try {
            if (stdIn.ready()) {
                String in = stdIn.readLine();
                if (in != null) {
                    String[] data = in.replace("(", "").replace(")", "").split(",");
                    // data[0], data[3] should be in radians.
                    BIGData.updateLidar(Double.parseDouble(data[0]), Double.parseDouble(data[1]),
                            Double.parseDouble(data[2]), Double.parseDouble(data[3]));
                    System.out.println(Arrays.toString(data));
                }
            }
        } catch (IOException e) {
        } catch (NullPointerException e) {
            System.out.println("unable to parse lidar data, NullPointerException");
        } catch (NumberFormatException e) {
            System.out.println("unable to parse lidar data, NumberFormatException");
        } catch (Exception e) {
            System.out.println("UNKNOWN ERROR: Attempted to read lidar data but something bad happened.");
        }

    }

}