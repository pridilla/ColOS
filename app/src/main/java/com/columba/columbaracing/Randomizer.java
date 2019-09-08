package com.columba.columbaracing;

import java.util.Random;

class Randomizer {
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

    boolean setData() {
        Random random = new Random();
        //if (input[10] != 32 || input[11] != 32 || input[12] != 32) return false;
        batteryPercentage = (short) random.nextInt(1024);
        degrees = (short) (random.nextInt(180)-90);
        tilted = random.nextBoolean();
        isBelowTreshold = random.nextBoolean();
        level = (short) (random.nextInt(180)-90);
        throttle = (byte) (random.nextInt(200)-100);
        steer = (byte) (random.nextInt(180)-90);
        sensorData = new byte[2][4];
        for (int i = 0; i < 2; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                sensorData[i][j] = (byte) (random.nextInt(256)-128);
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