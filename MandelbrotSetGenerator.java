import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MandelbrotSetGenerator {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int MAX_ITER = 1000; // Maximum number of iterations
    private static final double ZOOM = 1000;
    private BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

    public void generateMandelbrotSet() {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            int startY = i * (HEIGHT / numThreads);
            int endY = (i + 1) * (HEIGHT / numThreads);
            executor.submit(new MandelbrotTask(startY, endY));
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait for all threads to finish
        }

        saveImage("mandelbrot_set.png");
    }

    private void saveImage(String filename) {
        try {
            ImageIO.write(image, "png", new File(filename));
            System.out.println("Mandelbrot Set image saved as " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MandelbrotTask implements Runnable {
        private int startY, endY;

        public MandelbrotTask(int startY, int endY) {
            this.startY = startY;
            this.endY = endY;
        }

        @Override
        public void run() {
            for (int y = startY; y < endY; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    double zx, zy, cX, cY, tmp;
                    zx = zy = 0;
                    cX = (x - 400) / ZOOM;
                    cY = (y - 300) / ZOOM;
                    int iter = MAX_ITER;
                    while (zx * zx + zy * zy < 4 && iter > 0) {
                        tmp = zx * zx - zy * zy + cX;
                        zy = 2.0 * zx * zy + cY;
                        zx = tmp;
                        iter--;
                    }
                    image.setRGB(x, y, iter | (iter << 8));
                }
            }
        }
    }

    public static void main(String[] args) {
        new MandelbrotSetGenerator().generateMandelbrotSet();
    }
}
