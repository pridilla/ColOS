package com.columba.columbaracing;

class DecodeBytes {
    private static short batteryPercentage;
    private short degrees;
    private boolean tilted;
    private boolean isBelowTreshold;
    private short level;
    private byte throttle;
    private byte steer;
    byte[][] sensorData;

    double getBatteryPercentage() {
        double f = ((float) batteryPercentage )/614.4f;
        return (1/(Math.pow(1.872f,(66.21f - f/ 0.01854f)) + 1) + 0.002f);
    }

    short getDegrees() {
        return degrees;
    }

    boolean getTilted() {
        return tilted;
    }

    boolean getBelowTreshold() {
        return isBelowTreshold;
    }

    short getLevel() {
        return level;
    }

    byte getThrottle() {
        return throttle;
    }

    byte getSteer() {
        return steer;
    }

    byte[][] getSensorData() {
        return sensorData;
    }

    boolean setData(byte[] input) {
        if (input == null) return false;
        //if (input[10] != 32 || input[11] != 32 || input[12] != 32) return false;
        batteryPercentage = parseShort(input[1], input[0]);
        degrees = parseShort(input[3], input[2]);
        tilted = parseBoolean(input[4]);
        isBelowTreshold = parseBoolean(input[5]);
        level = parseShort(input[7], input[6]);
        throttle = input[8];
        steer = input[9];
        sensorData = new byte[2][4];
        for (int i = 0; i < 2; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                sensorData[i][j] = input[10 + 4*i + j];
            }
        }
        return true;
    }

    boolean parseBoolean(byte b) {
        return b != 0;
    }

    short parseShort(byte hi, byte lo) {
        return (short) (((hi & 0xFF) << 8) | (lo & 0xFF));
    }
}

class EncodeBytes {
    private byte chassisHeight;
    private byte disableAStr;

    void setChassisHeight(int x) {
        chassisHeight = (byte) x;
    }
    byte getChassisHeight() { return chassisHeight; }

    void setDisableAStr(int x) {
        disableAStr = (byte) x;
    }

    /*byte booleanToByte(boolean x) {
        return x == false ? (byte) 0 : (byte) 1;
    }*/

    byte[] getData() {
        return new byte[]{chassisHeight, disableAStr};
    }
}