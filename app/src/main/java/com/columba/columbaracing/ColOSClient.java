package com.columba.columbaracing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.AbstractQueue;
import java.util.LinkedList;
import java.util.Queue;

enum SteerMode {
    Normal, LockRear, Crab
}

enum BeamsMode {
    Auto, ManualOff, ManualOn
}

interface ColOSPacketListener {

    void dataReceived(byte[] data);
}

/**
 *
 * @author Juraj Marcin
 */
public class ColOSClient {

    // SYSTEM IDS //
    private final byte LXS = (byte) (0b001 << 5);
    private final byte SRC = (byte) (0b010 << 5);
    private final byte TAL = (byte) (0b011 << 5);
    private final byte CHS = (byte) (0b100 << 5);

    // LIGHTS IDS //
    private final byte LXS_SETTHRESHOLD = LXS | 0b01 << 3;
    private final byte LXS_BEAMS = LXS | 0b10 << 3;
    
    private final byte LXS_BEAMS_AUTO = LXS_BEAMS | 0b000;
    private final byte LXS_BEAMS_OFF = LXS_BEAMS | 0b010;
    private final byte LXS_BEAMS_ON = LXS_BEAMS | 0b011;

    // SPEKTRUMRC IDS //
    private final byte SRC_MODE = SRC | 0b00 << 3;
    
    private final byte SRC_MODE_NORMAL = SRC_MODE | 0b000;
    private final byte SRC_MODE_LOCK = SRC_MODE | 0b001;
    private final byte SRC_MODE_CRAB = SRC_MODE | 0b010;

    // TILTALARM IDS //
    private final byte TAL_STATE = TAL | 0b00 << 3;
    
    private final byte TAL_STATE_OFF = TAL_STATE | 0b000;
    private final byte TAL_STATE_ON = TAL_STATE | 0b001;

    // CHASSIS IDS //
    private final byte CHS_STEPUP = CHS | 0b10 << 3;
    private final byte CHS_STEPDOWN = CHS | 0b11 << 3;
    
    private ReadColOSSocket rcoss;
    private WriteColOSSocket wcoss;
    
    private DatagramSocket socket;
    
    ColOSClient(String serverAddress, int serverPort) throws Exception {
        socket = new DatagramSocket();
        
        rcoss = new ReadColOSSocket(socket, 50);
        wcoss = new WriteColOSSocket(socket, 50, serverAddress, serverPort);
        
        rcoss.start();
        wcoss.start();
        
        wcoss.sendPacket((byte) 0b11100000);
    }
    
    void setReceivedListener(ColOSPacketListener receivedListener) {
        rcoss.setListener(receivedListener);
    }
    
    byte[] readData() {
        return rcoss.getData();
    }

    //
    //  READ
    //    
    byte getBatteryPercentage() {
        double f = ((float) parseShort(readData()[1], readData()[0]) )/614.4f;
        return (byte)((1/(Math.pow(1.872f,(66.21f - f/ 0.01854f)) + 1) + 0.002f) * 100);
    }
    
    short getDegrees() {
        return parseShort(readData()[3], readData()[2]);
    }
    
    boolean getTilted() {
        return parseBoolean(readData()[4]);
    }
    
    boolean getBelowThreshold() {
        return parseBoolean(readData()[5]);
    }
    
    short getLevel() {
        return parseShort(readData()[7], readData()[6]);
    }
    
    byte getThrottle() {
        return readData()[8];
    }
    
    byte getSteer() {
        return readData()[9];
    }
    
    short[] getSensorData() {
        return new short[]{ (short) readData()[12], (short) readData()[13], (short) readData()[14], (short) readData()[15]};
    }
    
    SteerMode getSteerMode() {
        if (readData()[10] < 0) {
            return SteerMode.Crab;
        } else if (readData()[10] > 0) {
            return SteerMode.Normal;
        } else {
            return SteerMode.LockRear;
        }
    }
    
    BeamsMode getBeamsMode() {
        switch (readData()[11]) {
            case 0:
                return BeamsMode.Auto;
            case 2:
                return BeamsMode.ManualOff;
            default:
                return BeamsMode.ManualOn;
        }
    }

    //
    //  WRITE
    //
    // LIGHTS //
    void setLightThreshold() {
        wcoss.sendPacket(LXS_SETTHRESHOLD);
    }
    
    void setBeamsMode(BeamsMode mode) {
        if (mode == BeamsMode.Auto) {
            wcoss.sendPacket(LXS_BEAMS_AUTO);
        } else if (mode == BeamsMode.ManualOff) {
            wcoss.sendPacket(LXS_BEAMS_OFF);
        } else if (mode == BeamsMode.ManualOn) {
            wcoss.sendPacket(LXS_BEAMS_ON);
        }
    }

    // SPEKTRUMRC //
    void setSteerMode(SteerMode mode) {
        if (mode == SteerMode.Normal) {
            wcoss.sendPacket(SRC_MODE_NORMAL);
        } else if (mode == SteerMode.LockRear) {
            wcoss.sendPacket(SRC_MODE_LOCK);
        } else if (mode == SteerMode.Crab) {
            wcoss.sendPacket(SRC_MODE_CRAB);
        }
    }

    // TITLALARM //
    void setEnabled(boolean enabled) {
        if (enabled) {
            wcoss.sendPacket(TAL_STATE_ON);
        } else {
            wcoss.sendPacket(TAL_STATE_OFF);
        }
    }

    // CHASSIS //
    void setHeight(byte direction, byte speed) {
        if (direction > 0) {
            wcoss.sendPacket((byte) (CHS_STEPUP | (speed & 0b111)));
        } else {
            wcoss.sendPacket((byte) (CHS_STEPDOWN | (speed & 0b111)));
        }
    }

    // MISC //
    boolean connected() {
        return !socket.isClosed();
    }
    
    void stopThreads() {
        socket.close();
    }
    
    boolean parseBoolean(byte b) {
        return b != 0;
    }
    
    short parseShort(byte hi, byte lo) {
        return (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
    }
}

class ReadColOSSocket extends Thread {
    
    private ColOSPacketListener listener;
    
    private DatagramSocket socket;
    private int time;
    
    ReadColOSSocket(DatagramSocket s, int t) {
        socket = s;
        time = t;
        toRead = new byte[13];
        for (int i = 0; i < 13; i++)
        {
            toRead[i] = 0;
        }
    }
    
    private byte[] toRead;
    
    byte[] getData() {
        return toRead;
    }
    
    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                byte[] inData = new byte[13];
                DatagramPacket inPacket = new DatagramPacket(inData, inData.length);
                socket.receive(inPacket);
                toRead = inPacket.getData();
                System.out.print("DATA RECEIVED FROM " + inPacket.getAddress() + ":" + inPacket.getPort());
                for (int i = 0; i < inData.length; i++) {
                    System.out.print(' ');
                    System.out.print(inData[i]);
                }
                System.out.println("");
                if (listener != null) {
                    listener.dataReceived(toRead);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void setListener(ColOSPacketListener listener) {
        this.listener = listener;
    }
}

class WriteColOSSocket extends Thread {
    
    private DatagramSocket socket;
    int time;
    String addr;
    int port;
    
    WriteColOSSocket(DatagramSocket s, int t, String serverAddress, int serverPort) {
        socket = s;
        time = t;
        addr = serverAddress;
        port = serverPort;
        writeBuffer = new LinkedList<Byte>();
    }
    
    private Queue<Byte> writeBuffer;
    
    void sendPacket(byte mes) {
        writeBuffer.add(mes);
    }
    
    @Override
    public void run() {
        boolean err = false;
        while (!err && !socket.isClosed()) {
            if (!writeBuffer.isEmpty()) {
                try {
                    byte[] outData = new byte[1];
                    outData[0] = writeBuffer.peek();
                    DatagramPacket outPacket = new DatagramPacket(outData, outData.length, InetAddress.getByName(addr), port);
                    System.out.println("SENDING to " + outPacket.getAddress() + ":" + outPacket.getPort());
                    socket.send(outPacket);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    err = true;
                } finally {
                    writeBuffer.poll();
                }
            }
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
