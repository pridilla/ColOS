package com.columba.columbaracing;
import android.util.Log;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

class UDPClientSocket {
    boolean bstop = false;

    private DatagramSocket socket;

    private ReadUDPSocketThread rst;
    private WriteUDPSocketThread wst;

    UDPClientSocket(String serverAddress, int serverPort) throws Exception {
        socket = new DatagramSocket();
        rst = new ReadUDPSocketThread(socket, 50);
        wst = new WriteUDPSocketThread(socket, 50, serverAddress, serverPort);
    }

    boolean startSocket()
    {
        if (rst == null || wst == null) return false;
        rst.start();
        wst.start();
        return true;
    }

    byte[] readData() {
        return rst.getData();
    }

    void writeData(byte[] inData) {
        wst.setData(inData);
    }

    boolean connected() {
        if (rst.isInterrupted() || wst.isInterrupted()) return false;
        return true;
    }

    void stopThreads() {
        socket.close();
        wst.interrupt();
        rst.interrupt();
    }
}

class ReadUDPSocketThread extends Thread {
    private DatagramSocket socket;
    private int time;
    private byte[] toRead;

    ReadUDPSocketThread(DatagramSocket s, int t)
    {
        time = t;
        socket = s;
    }

    public void run()
    {
        while (!socket.isClosed()) {
            byte[] inData = new byte[18];
            DatagramPacket inPacket = new DatagramPacket(inData, inData.length);
            try {
                socket.receive(inPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            toRead = inPacket.getData();
            Log.d("SOCKET", Arrays.toString(toRead));

            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] getData() {
        return toRead;
    }
}



class WriteUDPSocketThread extends Thread {
    private DatagramSocket socket;
    private int time;
    private String addr;
    private int port;

    WriteUDPSocketThread(DatagramSocket s, int t, String serverAddress, int serverPort) {
        Log.d("SERVER", "WST Spustené");
        socket = s;
        time = t;
        addr = serverAddress;
        port = serverPort;
    }

    private byte[] toWrite;

    void setData(byte[] mes) {
        toWrite = mes;
    }

    @Override
    public void run() {
        boolean err = false;
        while (!err && !socket.isClosed()) {
            if (toWrite != null) {
                try {
                    byte[] outData;
                    outData = toWrite;
                    DatagramPacket outPacket = new DatagramPacket(outData, outData.length, InetAddress.getByName(addr), port);
                    socket.send(outPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                    err = true;
                } finally {
                    toWrite = null;
                }
            }
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class LocalView{
    static final int TILT = 1;
    static final int AXLE = 2;
    static final int SPEED_NEEDLE = 4;
    static final int HEIGHT = 5;
    static final int LIGHT = 6;
    static final int SETTINGS = 7;
    static final int BATTERY = 8;
    static final int MOD = 10;
    static final int STEER_NEEDLE = 12;
    static final int STEER_RECTANGLE = 13;

    static final int OBSTACLE_LEFT = 21;
    static final int OBSTACLE_RIGHT = 22;
}

class Profiles{
    static final int M = 0;
    static final int S = 1;
    static final int H = 2;
    static final int R = 3;
    static final int P = 4;
}

class Draco_Profiles{
    static final int LOCKED = 0;
    static final int CRAB = -1;
    static final int MIRROR = 1;
}