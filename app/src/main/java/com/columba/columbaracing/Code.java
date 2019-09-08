package com.columba.columbaracing;

class Decode {
    private static byte batteryPercentage;
    private short degrees;
    private boolean tilted;
    private boolean isBelowTreshold;
    private short level;
    private byte throttle;
    private byte steer;
    short[] sensorData;

    byte getBatteryPercentage(){return batteryPercentage;}
    short getDegrees(){return degrees;}
    boolean getTilted(){return tilted;}
    boolean getBelowTreshold(){return isBelowTreshold;}
    short getLevel(){return level;}
    byte getThrottle(){return throttle;}
    byte getSteer(){return steer;}
    short[] getSensorData(){return sensorData;}

    boolean setData(String input)
    {
        if (input.length() != 50) return false;
        batteryPercentage = (byte)parseHexBinary(input.substring(0,2));
        degrees = (short)parseHexBinary(input.substring(2,6));
        tilted = parseIntBoolean(parseHexBinary(input.substring(6,8)));
        isBelowTreshold = parseIntBoolean(parseHexBinary(input.substring(8,10)));
        level = (short)parseHexBinary(input.substring(10,14));
        throttle = (byte)parseHexBinary(input.substring(14,16));
        steer = (byte)parseHexBinary(input.substring(16,18));
        sensorData = new short[8];
        for (int i = 0; i < 8; i++)
        {sensorData[i] = (short)parseHexBinary(input.substring(18 + 4*i,22+4*i));}
        return true;
    }

    private int parseHexBinary(String s) {
        if( s.length()%2 != 0 )
            throw new IllegalArgumentException("hexBinary needs to be even-length: "+s);
        int out = 0;
        for (int i = 0; i < s.length(); i++)
        {
            out += hexToBin(s.charAt(i))*Math.pow(16, s.length()-i-1);
        }
        int tresh = (int) Math.pow(2,s.length()*4)/2;
        if (out >= tresh)
        {
            out = out-tresh*2;
        }
        return out;
    }

    private static int hexToBin( char ch ) {
        if( '0'<=ch && ch<='9' )    return ch-'0';
        if( 'A'<=ch && ch<='F' )    return ch-'A'+10;
        if( 'a'<=ch && ch<='f' )    return ch-'a'+10;
        return -1;
    }

    private boolean parseIntBoolean(int i)
    {
        return i != 0;
    }

}

class Encode
{
    private static byte batteryPercentage;
    private short degrees;
    private boolean tilted;
    private boolean isBelowTreshold;
    private short level;
    private byte throttle;
    private byte steer;
    short[] sensorData;

    void setBatteryPercentage(int x) {batteryPercentage = (byte) x;}
    void setDegrees(int x){degrees = (short)x;}
    void setTilted(boolean x) {tilted = x;}
    void setBelowTreshold(boolean x) {isBelowTreshold = x;}
    void setLevel(int x) {level = (short) x;}
    void setThrottle(int x) {throttle = (byte) x;}
    void setSteer(int x) {steer = (byte) x;}
    void setSensorData(short[] x) {sensorData = x;}

    String shortToHex(short x)
    {
        String t = Integer.toHexString(x & 0xffff);
        String s = "";
        for (int i = 0; i < 4 - t.length(); i++)
        {
            s += '0';
        }
        return s + t;
    }
    String byteToHex(byte x)
    {
        String t = Integer.toHexString(x & 0xff);
        String s = "";
        for (int i = 0; i < 2 - t.length(); i++)
        {
            s += '0';
        }
        return s + t;
    }
    String booleanToHex(boolean x) { if(x) return "01"; return "00"; }

    String getData()
    {
        String result = byteToHex(batteryPercentage) + shortToHex(degrees) + booleanToHex(tilted) + booleanToHex(isBelowTreshold) + shortToHex(level) + byteToHex(throttle) + byteToHex(steer);
        for (int i = 0; i < 8; i++)
        {
            result += shortToHex(sensorData[i]);
        }
        return result;
    }
}