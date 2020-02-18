package frc.control;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import com.google.gson.Gson;

import frc.gen.BIGData;

public class FieldGUI {
    private String address;
    private boolean startedTimer = false;

    public FieldGUI(String ip, int port) {
        this.address = String.format("http://%1$s:%2$d/", ip, port);
    }

    public void update() {
        String buttonClick = goGet("buttondata");
        BIGData.putButtonClick(buttonClick);

        String canvasClick = goGet("canvasdata");
        BIGData.putCanvasClick(canvasClick);

        System.out.println("Button clicked: " + BIGData.getButtonClick());
        System.out.println("Canvas spot clicked: " + BIGData.getCanvasClick());
        
        if (!startedTimer && BIGData.getBoolean("auton_started")) {
            goPost("starttimer", "true");
            startedTimer = true;
        }

        HashMap<String, Integer> working = new HashMap<>();
        working.put("fl", (int) Math.round(Math.random()));
        working.put("fr", (int) Math.round(Math.random()));
        working.put("bl", (int) Math.round(Math.random()));
        working.put("br", (int) Math.round(Math.random()));
        goPost("swervedata", new Gson().toJson(working));

        goPost("angledata", Double.toString(BIGData.getGyroAngle()));
        goPost("lemondata", Integer.toString(BIGData.getInt("lemon_count")));
        //goPost("getlidar", new Gson().toJson(Target.convert()));
    }

    public void goPost(String page, String inputData) {
        HttpURLConnection conn = null;
        DataOutputStream os = null;
        // Connect to the flask page requested with whatever input
        try {
            URL url = new URL(address + page + "/"); // important to add the trailing slash
            byte[] postData = inputData.getBytes(StandardCharsets.UTF_8);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(inputData.length()));
            os = new DataOutputStream(conn.getOutputStream());
            os.write(postData);
            os.flush();

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public String goGet(String page) {
        HttpURLConnection conn = null;
        StringBuilder sb = new StringBuilder();
        // Connect to the flask page requested
        try {
            URL url = new URL(address + page + "/"); // important to add the trailing slash
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return sb.toString();
    }
}