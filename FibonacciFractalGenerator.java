import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FibonacciFractalGenerator {

    private static final double GOLDEN_RATIO = (1 + Math.sqrt(5)) / 2; // Phi
    private static final int WIDTH = 2000;
    private static final int HEIGHT = 2000;
    private BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

    public void generateFractal(int maxElement) {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        int[] fibonacci = new int[maxElement];
        //int[] fibonacci2 = calculateFibonacci(maxElement);

        int threadGroupSize = maxElement / numThreads;
        Future<?>[] futureFibTasks = new Future[numThreads]; // kinda like promises in JS

        for (int i = 0; i < numThreads; i++) {
            int start = i * threadGroupSize;
            int end = (i + 1) * threadGroupSize;

            if (i == numThreads - 1) { // odd number of elements so pick up the slack
                end = maxElement;
            }

            // init the first two elements of the fibonacci sequence
            fibonacci[start] = calculateNthFibonacci(start);
            fibonacci[start + 1] = calculateNthFibonacci(start + 1);
            futureFibTasks[i] = executor.submit(new CalculateFibonacciTask(fibonacci, start, end));
        }

        do {
            int doneThreads = 0;

            for (int i = 0; i < numThreads; i++) {
                if (futureFibTasks[i].isDone()) {
                    doneThreads++;
                }
            }

            if (doneThreads == numThreads) {
                break;
            }
        } while (true);

        System.out.println("Fibonacci sequence calculated");

        long currentTime = System.currentTimeMillis();

        // ---- Draw the fractal ----
        // TODO: Figure out how to do this in parallel. Might have to do it in reverse order.

        int centerX = WIDTH / 2;
        int centerY = HEIGHT / 2;

        int x = 0;
        int y = 0;
        int angle = 0;
        int largestFib = fibonacci[maxElement - 1];
        int largestFib2 = fibonacci[maxElement - 2];
        double scale = 1.0 * WIDTH / (largestFib2 + largestFib);

        for (int i = 0; i < maxElement; i++) {
            int currentFib = fibonacci[i];
            int previousFib = i - 1 < 0 ? 0 : fibonacci[i - 1];
            int previousPreviousFib = i - 2 < 0 ? 0 : fibonacci[i - 2];

            // Calculate next position and angle

            int[] deltaXY = new int[2];

            switch (angle) {
                case 0:
                    deltaXY[0] = -previousPreviousFib;
                    deltaXY[1] = -currentFib;
                    break;
                case 90:
                    deltaXY[0] = -currentFib;
                    break;
                case 180:
                    deltaXY[1] = previousFib;
                    break;
                case 270:
                    deltaXY[0] = previousFib;
                    deltaXY[1] = -previousPreviousFib;
                    break;
            }

            System.out.println(currentFib + ", " + previousFib + ", " + previousPreviousFib);
            System.out.println("Angle: " + angle);
            System.out.println("DeltaXY: " + deltaXY[0] + ", " + deltaXY[1]);

            x += deltaXY[0];
            y += deltaXY[1];

            double scaledX = (x * scale) + (centerX);
            double scaledY = (y * scale) + (centerY);
            double scaledFib = (currentFib * scale);

            if (scaledFib != 0)
                executor.submit(new FractalDrawingTask(image, scaledX, scaledY, angle, scaledFib, currentFib));

            angle += 90;
            angle %= 360;
        }

        executor.shutdown();

        while (!executor.isTerminated()) {
            // Wait for all threads to finish
        }

        Graphics2D graphics = (Graphics2D) image.getGraphics();

        graphics.setColor(Color.magenta);
        graphics.fillRect(WIDTH / 2, HEIGHT / 2, 1, 1);
        graphics.dispose();

        saveImage("fibonacci_fractal.png");

        System.out.println("Image took " + (System.currentTimeMillis() - currentTime) + "ms to generate.");
    }

    /* F(n) = round( Phi^n / √5 ) provided n ≥ 0
    modified with an offset to start from 1
      n:    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, ...
      fib:  1, 1, 2, 3, 5, 8, 13, 21, 34, 55, ...
     */
    private int calculateNthFibonacci(int n) {
        assert n >= 0;

        return (int) Math.round(Math.pow(GOLDEN_RATIO, n + 1) / Math.sqrt(5));
    }

    private int[] calculateFibonacci(int maxElement) {
        int[] fibonacci = new int[maxElement];

        fibonacci[0] = 1;
        fibonacci[1] = 1;

        for (int i = 2; i < maxElement; i++) {
            fibonacci[i] = fibonacci[i - 1] + fibonacci[i - 2];
        }

        return fibonacci;
    }

    static class CalculateFibonacciTask implements Runnable {

        private final int[] fibonacci;
        private final int start;
        private final int end;

        public CalculateFibonacciTask(int[] fibonacci, int start, int end) {
            this.fibonacci = fibonacci;
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            for (int i = start + 2; i < end; i++) {
                fibonacci[i] = fibonacci[i - 1] + fibonacci[i - 2];
                System.err.println(fibonacci[i]);
            }
        }

    }

    private void saveImage(String fileName) {
        try {
            ImageIO.write(image, "png", new File(fileName));

            System.out.println("Fibonacci fractal image saved as " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class FractalDrawingTask implements Runnable {

        private final BufferedImage image;
        private final double x;
        private final double y;
        private final int angle;
        private final double scaledFib;
        private final double currentFib;

        public FractalDrawingTask(BufferedImage image, double x, double y, int angle, double scaledFib, double currentFib) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.scaledFib = scaledFib;
            this.currentFib = currentFib;
        }

        @Override
        public void run() {
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.BLUE);

            synchronized (image) {
                // draw a dot at the center of the image
                double arcX = x;
                double arcY = y;

                if (angle == 0) {
                    arcX = x - scaledFib;
                    arcY = y;
                    graphics.setColor(Color.GRAY);
                } else if (angle == 90) {
                    arcX = x;
                    arcY = y;
                    graphics.setColor(Color.WHITE);
                } else if (angle == 180) {
                    arcX = x;
                    arcY = y - scaledFib;
                    graphics.setColor(Color.ORANGE);
                } else if (angle == 270) {
                    arcX = x - scaledFib;
                    arcY = y - scaledFib;
                    graphics.setColor(Color.GREEN);
                }

                Shape shape = new BigIntRectangle(toBigInt(x), toBigInt(y), toBigInt(scaledFib), toBigInt(scaledFib));

                graphics.draw(shape);
                graphics.setColor(Color.RED);

                Arc2D arc = new Arc2D.Double(arcX, arcY, 2 * scaledFib - 1, 2 * scaledFib - 1, angle, 90, Arc2D.OPEN);

                graphics.draw(arc);

                graphics.drawString(Double.toString(currentFib), (int) (x + scaledFib / 2), (int) (y + scaledFib / 2));
            }

            graphics.dispose();
        }
    }

    public static BigInteger toBigInt(int number) {
        return BigInteger.valueOf(number);
    }

    public static BigInteger toBigInt(double number) {
        return BigInteger.valueOf((long) number);
    }
    public static void main(String[] args) {
        new FibonacciFractalGenerator().generateFractal(20);
    }

}
