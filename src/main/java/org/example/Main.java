package org.example;

import java.util.Random;

public class Main {
    private static final int NUM_POINTS = 1000000;
    private static final int NUM_THREADS = 4;

    private static double calculatePiSingleThread() {
        int insideCircle = 0;
        Random random = new Random();

        for (int i = 0; i < Main.NUM_POINTS; i++) {
            double x = random.nextDouble();
            double y = random.nextDouble();
            double distance = x * x + y * y;

            if (distance <= 1) {
                insideCircle++;
            }
        }

        return 4.0 * insideCircle / Main.NUM_POINTS;
    }

    private static double calculatePiMultiThread() {
        int pointsPerThread = Main.NUM_POINTS / Main.NUM_THREADS;
        PiCalculator[] calculators = new PiCalculator[Main.NUM_THREADS];
        Thread[] threads = new Thread[Main.NUM_THREADS];

        for (int i = 0; i < Main.NUM_THREADS; i++) {
            calculators[i] = new PiCalculator(pointsPerThread);
            threads[i] = new Thread(calculators[i]);
            threads[i].start();
        }

        int totalInsideCircle = 0;
        for (int i = 0; i < Main.NUM_THREADS; i++) {
            synchronized (calculators[i]) {
                while (!calculators[i].isFinished()) {
                    try {
                        calculators[i].wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                totalInsideCircle += calculators[i].getInsideCircleCount();
            }
        }

        return 4.0 * totalInsideCircle / Main.NUM_POINTS;
    }

    private static class PiCalculator implements Runnable {
        private final int numPoints;
        private int insideCircleCount = 0;
        private boolean finished = false;
        private final Random random = new Random();

        public PiCalculator(int numPoints) {
            this.numPoints = numPoints;
        }

        @Override
        public void run() {
            for (int i = 0; i < numPoints; i++) {
                double x = random.nextDouble();
                double y = random.nextDouble();
                double distance = x * x + y * y;

                if (distance <= 1) {
                    insideCircleCount++;
                }
            }

            synchronized (this) {
                finished = true;
                notify();
            }
        }

        public int getInsideCircleCount() {
            return insideCircleCount;
        }

        public boolean isFinished() {
            return finished;
        }
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        double piSingleThread = calculatePiSingleThread();
        long endTime = System.currentTimeMillis();

        System.out.println("Однопоточний розрахунок Pi: " + piSingleThread);
        System.out.println("Час виконання однопоточного розрахунку: " + (endTime - startTime) + " мс");

        startTime = System.currentTimeMillis();
        double piMultiThread = calculatePiMultiThread();
        endTime = System.currentTimeMillis();

        System.out.println("Багатопоточний розрахунок Pi: " + piMultiThread);
        System.out.println("Час виконання багатопоточного розрахунку: " + (endTime - startTime) + " мс");
    }
}

