import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FibonacciFractalGenerator {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

    public void generateFractal(int maxElement) {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        //int[] fibonacci = calculateFibonacci(maxElement + 1);
       

        // Start from the center of the image
        int x = WIDTH / 2;
        int y = HEIGHT / 2;
        int angle = 0;

        
        int[] fibonacci = {13, 21, 34, 55, 89, 144, 233, 377, 610, 987};
        for (int i = 0; i < maxElement; i++) {
            int radius = fibonacci[i];
            executor.submit(new FractalDrawingTask(image, x, y, angle, radius));
            // Calculate next position and angle
            angle += 90;
            angle %= 360;
            if (i % 2 == 0) {
                y += fibonacci[i + 1] * (i % 4 == 0 ? 1 : -1);
            } else {
                x += fibonacci[i + 1] * (i % 4 == 1 ? -1 : 1);
            }

            
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
        private int x, y, angle, radius;

        public FractalDrawingTask(BufferedImage image, int x, int y, int angle, int radius) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.radius = radius;
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
                    arcX = x - radius;
                    arcY = y;
                } else if(angle == 90){
                    arcX = x;
                    arcY = y;
                } else if(angle == 180){
                    arcX = x;
                    arcY = y - radius;
                } else if(angle == 270){
                    arcX = x - radius;
                    arcY = y - radius;
                }
                g.fillRect(x, y, radius, radius);
                g.setColor(Color.RED);
                Arc2D arc = new Arc2D.Double(arcX, arcY, 2 * radius, 2 * radius, angle, 90, Arc2D.OPEN);
                g.draw(arc);
                System.err.println("Drawing arc at " + arcX + ", " + arcY + ", " + angle + ", " + radius);
                

                // draw a red square in the center of the arc
                
            }
            g.dispose();
        }
    }

    public static void main(String[] args) {
        new FibonacciFractalGenerator().generateFractal(3);
    }
}
