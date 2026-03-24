package bike_racing;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

class RiderStats {
    String name;
    long startTime;
    long endTime;
    long totalTime;

    public RiderStats(String name, long startTime, long endTime) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalTime = endTime - startTime;
    }
}

class RiderWorker implements Callable<RiderStats> {
    private final String name;
    private final int distance;
    private final CountDownLatch gate;

    public RiderWorker(String name, int distance, CountDownLatch gate) {
        this.name = name;
        this.distance = distance;
        this.gate = gate;
    }

    @Override
    public RiderStats call() throws Exception {
        gate.await();

        long start = System.currentTimeMillis();

        int speed = ThreadLocalRandom.current().nextInt(20, 40); // m/s
        int totalTimeMs = (distance * 1000) / speed;

        int checkpoints = 10;
        int stepDistance = distance / checkpoints;

        for (int i = 1; i <= checkpoints; i++) {
            Thread.sleep(totalTimeMs / checkpoints);

            int covered = i * stepDistance;
            if (covered > distance) covered = distance;

            synchronized (System.out) {
                System.out.println(name + " covered " + covered + "m");
            }
        }

        long end = System.currentTimeMillis();
        return new RiderStats(name, start, end);
    }
}

public class SarthakRaceArena {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.println("     WELCOME TO SARTHAK RACING ARENA   ");
        System.out.println("=========================================");

        List<String> riders = new ArrayList<>();

        System.out.println("Enter names of 10 Riders:");
        for (int i = 1; i <= 10; i++) {
            System.out.print("Rider " + i + ": ");
            String name = input.nextLine();
            if (name.trim().isEmpty()) name = "Rider-" + i;
            riders.add(name);
        }

        System.out.print("\nEnter race distance (KM): ");
        double km = input.nextDouble();
        int meters = (int) (km * 1000);

        ExecutorService pool = Executors.newFixedThreadPool(10);
        CountDownLatch startGate = new CountDownLatch(1);

        List<Callable<RiderStats>> tasks = new ArrayList<>();
        for (String r : riders) {
            tasks.add(new RiderWorker(r, meters, startGate));
        }

        try {
            List<Future<RiderStats>> futures = new ArrayList<>();
            for (Callable<RiderStats> t : tasks) {
                futures.add(pool.submit(t));
            }

            System.out.println("\nRiders ready...");
            Thread.sleep(1000);

            System.out.println("\n*** COUNTDOWN ***");
            for (int i = 10; i > 0; i--) {
                System.out.print(i + "...");
                Thread.sleep(500);
            }

            System.out.println("\n\nGO!\n");

            startGate.countDown();

            List<RiderStats> results = new ArrayList<>();
            for (Future<RiderStats> f : futures) {
                results.add(f.get());
            }

            showResults(results);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
            input.close();
        }
    }

    private static void showResults(List<RiderStats> results) {
        results.sort(Comparator.comparingLong(r -> r.totalTime));

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");

        System.out.println("\n================ FINAL RESULT =================");

        System.out.printf("%-5s | %-15s | %-15s | %-15s | %-10s%n",
                "RANK", "NAME", "START", "END", "TIME(s)");

        int rank = 1;

        for (RiderStats r : results) {
            String start = formatter.format(new Date(r.startTime));
            String end = formatter.format(new Date(r.endTime));
            double sec = r.totalTime / 1000.0;

            System.out.printf("%-5d | %-15s | %-15s | %-15s | %-10.3f%n",
                    rank++, r.name, start, end, sec);
        }
    }
}