import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.math.BigInteger;

public class FibonacciFractalGenerator {
    private static final double GOLDEN_RATIO = (1 + Math.sqrt(5)) / 2; // Phi
    private static final int WIDTH = 2000;
    private static final int HEIGHT = 2000;
    private BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

    public void generateFractal(int maxElement) {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        BigInteger[] fibonacci = new BigInteger[maxElement];
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
        
        do{ 
            int doneThreads = 0;
            for(int i = 0; i < numThreads; i++){
                if(futureFibTasks[i].isDone()){
                    doneThreads++;
                }
            }
            if(doneThreads == numThreads){
                break;
            }
        } while(true);
        
        saveFibonacciSequence(fibonacci, "fibonacci_sequence.txt");
        
        System.out.println("Fibonacci sequence calculated");
        
        
        // ---- Draw the fractal ----
        // TODO: Figure out how to do this in parallel. Might have to do it in reverse order.
        
        int centerX = WIDTH / 2;
        int centerY = HEIGHT / 2;
        
        int x = 0;
        int y = 0;
        int angle = 0;
        BigInteger largestFib = fibonacci[maxElement - 1];
        BigInteger largestFib2 = fibonacci[maxElement - 2];
        BigInteger sumOfLargestFibs = largestFib2.add(largestFib);
        double scale = 1.0 * WIDTH / sumOfLargestFibs.doubleValue();

        for (int i = 0; i < maxElement; i++) {
            BigInteger currentFib = fibonacci[i];
            BigInteger previousFib = i - 1 < 0 ? 0 : fibonacci[i - 1];
            BigInteger previousPreviousFib = i - 2 < 0 ? 0 : fibonacci[i - 2];
            
            // Calculate next position and angle
            
            BigInteger[] deltaXY = new BigInteger[2];
            //initialize deltaXY
            deltaXY[0] = BigInteger.ZERO;
            deltaXY[1] = BigInteger.ZERO;
            switch(angle){
                case 0:
                    deltaXY[0] = previousPreviousFib.negate();
                    deltaXY[1] = currentFib.negate();
                    break;
                case 90:
                    deltaXY[0] = currentFib.negate();
                    break;
                case 180:
                    deltaXY[1] = previousFib;
                    break;
                case 270:
                    deltaXY[0] = previousFib;
                    deltaXY[1] = previousPreviousFib.negate();
                    break;
            }
            
            System.out.println(currentFib + ", " + previousFib + ", " + previousPreviousFib);
            System.out.println("Angle: " + angle);  
            System.out.println("DeltaXY: " + deltaXY[0] + ", " + deltaXY[1]);
            x += deltaXY[0].intValue(); 
            y += deltaXY[1].intValue(); 

            int scaledX = (int)(x * scale) + (int)(centerX);
            int scaledY = (int)(y * scale) + (int)(centerY);
            int scaledFib = (int)(currentFib.intValue() * scale);
            if(scaledFib != 0)
                executor.submit(new FractalDrawingTask(image, scaledX, scaledY, angle, scaledFib, currentFib));

            angle += 90;
            angle %= 360;
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait for all threads to finish
        }
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(Color.magenta);
        g.fillRect(WIDTH / 2, HEIGHT /2, 1, 1);
        g.dispose();

        saveImage("fibonacci_fractal.png");
        
        
    }

    public void saveFibonacciSequence(BigInteger[] fibonacci, String filename) {
        try {
            File file = new File(filename);
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            for (int i = 0; i < fibonacci.length; i++) {
                writer.write(fibonacci[i] + "\n");
            }
            writer.close();
            System.out.println("Fibonacci sequence saved as " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /* F(n) = round( Phi^n / √5 ) provided n ≥ 0
    modified with an offset to start from 1
      n:    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, ...
      fib:  1, 1, 2, 3, 5, 8, 13, 21, 34, 55, ...
     */
    private BigInteger calculateNthFibonacci(int n) {
        assert n >= 0;
        return (BigInteger) Math.round(Math.pow(GOLDEN_RATIO, n + 1) / Math.sqrt(5));
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
        private int[] fibonacci;
        private int start, end;

        public CalculateFibonacciTask(BigInteger[] fibonacci2, int start, int end) {
            this.fibonacci = fibonacci2;
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

    private void saveImage(String filename) {
        try {
            ImageIO.write(image, "png", new File(filename));
            System.out.println("Fibonacci fractal image saved as " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class FractalDrawingTask implements Runnable {
        private BufferedImage image;
        private int x, y, angle, scaledFib, currentFib;

        public FractalDrawingTask(BufferedImage image, int x, int y, int angle, int scaledFib, BigInteger currentFib2) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.scaledFib = scaledFib;
            this.currentFib = currentFib2;
        }

        @Override
        public void run() {
            Graphics2D g = (Graphics2D) image.getGraphics();
            g.setColor(Color.BLUE);
            synchronized (image) {  
                // draw a dot at the center of the image
                              
                double arcX = x;
                double arcY = y;
                if(angle == 0){
                    arcX = x - scaledFib;
                    arcY = y;
                    g.setColor(Color.GRAY);
                } else if(angle == 90){
                    arcX = x;
                    arcY = y;
                    g.setColor(Color.WHITE);
                } else if(angle == 180){
                    arcX = x;
                    arcY = y - scaledFib;
                    g.setColor(Color.ORANGE);
                } else if(angle == 270){
                    arcX = x - scaledFib;
                    arcY = y - scaledFib;
                    g.setColor(Color.GREEN);
                }
                g.drawRect(x, y, scaledFib, scaledFib);
                g.setColor(Color.RED);
                Arc2D arc = new Arc2D.Double(arcX, arcY, 2 * scaledFib - 1, 2 * scaledFib - 1, angle, 90, Arc2D.OPEN);
                g.draw(arc);

                g.drawString(Integer.toString(currentFib), x + scaledFib/2, y + scaledFib/2);                
            }
            g.dispose();
        }
    }

    public static void main(String[] args) {
        new FibonacciFractalGenerator().generateFractal(2000);
    }
}
