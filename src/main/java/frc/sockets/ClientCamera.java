package frc.sockets;

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

public class ClientCamera implements Runnable {
    static Thread sent;
    static Thread receive;
    static Socket socket;
    static BufferedReader stdIn;
    static PrintWriter out;
    static Notifier notifier;

    public void run() {
        // System.out.println("IN CLIENT RUN");
        cameraData();
    }

    public ClientCamera() {
        connect();
    }

    public void connect() {
        System.out.println("start connect pls");
        try {
            InetAddress address = InetAddress.getByName("10.1.92.14");
            socket = new Socket(address, 30000);
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

    public void cameraData() {
        try {
            String in = stdIn.readLine();
            // System.out.println(in);
            String[] data = in.replace("(", "").replace(")", "").split(",");
            BIGData.updateCamera(Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]));
            // out.print("Recieved\n");
            out.flush();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
