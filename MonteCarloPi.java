import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Random;

public class MonteCarloPi {

    public static void main(String[] args) throws Exception {
        long currentTime = System.currentTimeMillis();

        int numThreads = 4; // Number of threads to use
        long numPointsPerThread = 1000000000; // Number of points per thread

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Create and submit tasks
        List<Future<Long>> futures = new ArrayList<>(numThreads);

        for (int i = 0; i < numThreads; i++) {
            futures.set(i, executor.submit(new MonteCarloTask(numPointsPerThread)));
        }

        // Collect and sum up results
        long totalInside = 0;
        for (Future<Long> future : futures) {
            totalInside += future.get();
        }

        // Shutdown the executor
        executor.shutdown();

        // Calculate Pi approximation
        double pi = 4.0 * totalInside / (numThreads * numPointsPerThread);
        System.out.println("Approximation of Pi: " + pi);

        System.out.println("Image took " + (System.currentTimeMillis() - currentTime) + "ms to generate.");
    }

    public static class MonteCarloTask implements Callable<Long> {

        private final long numPoints;

        public MonteCarloTask(long numPoints) {
            this.numPoints = numPoints;
        }

        @Override
        public Long call() {
            long inside = 0;
            Random random = new Random();

            for (long i = 0; i < numPoints; i++) {
                double x = random.nextDouble();
                double y = random.nextDouble();

                if (x * x + y * y <= 1) {
                    inside++;
                }
            }
            return inside;
        }

    }

}
