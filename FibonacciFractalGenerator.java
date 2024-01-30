import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FibonacciFractalGenerator {

    private static final int WIDTH = 2000;
    private static final int HEIGHT = 2000;
    private BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

    public void generateFractal(int maxElement) {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        int[] fibonacci = calculateFibonacci(maxElement + 1);
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
            //initialize deltaXY
            deltaXY[0] = 0;
            deltaXY[1] = 0;
            switch(angle){
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

            int scaledX = (int)(x * scale) + (int)(centerX);
            int scaledY = (int)(y * scale) + (int)(centerY);
            int scaledFib = (int)(currentFib * scale);
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

    private int[] calculateFibonacci(int maxElement) {
        int[] fibonacci = new int[maxElement];
        fibonacci[0] = 1;
        fibonacci[1] = 1;
        for (int i = 2; i < maxElement; i++) {
            fibonacci[i] = fibonacci[i - 1] + fibonacci[i - 2];
        }
        return fibonacci;
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

        public FractalDrawingTask(BufferedImage image, int x, int y, int angle, int scaledFib, int currentFib) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.scaledFib = scaledFib;
            this.currentFib = currentFib;
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
        new FibonacciFractalGenerator().generateFractal(40);
    }
}
