package com.columba.columbaracing;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientSocket {

    private ReadSocketThread rst;
    private WriteSocketThread wst;

    ClientSocket(String serverAddress, int serverPort) throws Exception{
        Socket socket = new Socket(serverAddress, serverPort);

        rst = new ReadSocketThread(socket, 50);
        wst = new WriteSocketThread(socket, 50);

        rst.start();
        wst.start();
    }

    String readData()
    {
        return rst.getData();
    }

    void writeData(String inData)
    {
        wst.setData(inData);
    }
}

class ReadSocketThread extends Thread
{
    private Socket socket;
    private int time;
    ReadSocketThread(Socket s, int t)
    {
        socket = s;
        time = t;
    }

    private String toRead;
    String getData() { return toRead; }

    @Override
    public void run()
    {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String data;
            while ((data = in.readLine()) != null)
            {
                toRead = data;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class WriteSocketThread extends Thread
{
    private Socket socket;
    int time;
    WriteSocketThread(Socket s, int t)
    {
        socket = s;
        time = t;
    }

    private String toWrite;
    void setData(String mes) {toWrite = mes;}

    @Override
    public void run()
    {
        boolean err = false;
        while (!err)
        {
            if (toWrite != null){
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(toWrite);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    err = true;
                }
                finally {
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
