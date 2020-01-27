package frc.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.wpi.first.wpilibj.Notifier;
import frc.gen.BIGData;

public class ClientLidar implements Runnable {
    static Thread sent;
    static Thread receive;
    static Socket socket;
    static BufferedReader stdIn;
    static PrintWriter out;
    static Notifier notifier;

    public void run() {
        // System.out.println("IN CLIENT RUN");
        lidarData();
    }

    public ClientLidar() {
        connect();
    }

    public void connect() {
        System.out.println("start connect pls");
        try {
            InetAddress address = InetAddress.getByName("10.1.92.14");
            socket = new Socket(address, 1030);
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        System.out.println("omg did i connect? uwu");
        try {
            stdIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        notifier = new Notifier(this);
        notifier.startPeriodic(0.02);
    }

    public void lidarData() {
        try {
            String in = stdIn.readLine();
            System.out.println(in);
            BIGData.updateLidar(Double.parseDouble(in));
            // out.print("Recieved\n");
            out.flush();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
