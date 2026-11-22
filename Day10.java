import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

record Point (int y, int x) {
    public double angle90(Point from) {
        double dy = y() - from.y();
        double dx = x() - from.x();
        double angle = Math.toDegrees(Math.atan2(dx, -dy));
        return (angle + 360.0) % 360.0;
    }

    public long dist_from(Point from) {
        long yy = (long) this.y - from.y();
        long xx = (long) this.x - from.x();
        return yy * yy + xx * xx;
    }
}

record RankedPoint(Point point, int rotationIndex) {}

public class Day10 {
    static List<Point> asteroids;

    static int gcf (int a, int b) {
        return b == 0 ? Math.abs(a) : gcf(b, a % b);
    }

    static Point slope(Point a, Point b) {
        int yd = b.y() - a.y();
        int xd = b.x() - a.x();
        int f = gcf(yd, xd);
        return new Point(yd / f, xd / f);
    }

    static Comparator<RankedPoint> comparatorFor (Point station) { 
        return Comparator
            .comparingInt(RankedPoint::rotationIndex)
            .thenComparingDouble(rp -> rp.point().angle90(station));
    }

    static Comparator<Point> distanceFrom(Point station) {
        return Comparator.comparingLong(p -> p.dist_from(station));
    }

    public static void main(String[] args) throws IOException {
        File file = new File("./10.txt");
        FileReader reader = new FileReader(file);
        List<String> lines = reader.readAllLines();

        int N = lines.size(), M = lines.get(0).length();
        asteroids = new ArrayList<>();
        for (int y = 0; y < N; y++) {
            for (int x = 0; x < M; x++) {
                if (lines.get(y).charAt(x) == '#') {
                    asteroids.add(new Point(y, x));
                }
            }
        }

        int best = 0;
        Map<Point, List<Point>> from_station = null;
        Point station = null;

        for (Point a : asteroids) {
            Map<Point, List<Point>> slopes = new HashMap<>();
            for (Point b : asteroids) {
                if (a.equals(b)) 
                    continue;
                Point m = slope(a, b);
                slopes.computeIfAbsent(m, k -> new ArrayList<>()).add(b);
            }
            if (slopes.size() > best) {
                from_station = slopes;
                best = slopes.size();
                station = a;
            }
        }

        System.out.println(best);
        
        PriorityQueue<RankedPoint> pq = new PriorityQueue<>(comparatorFor(station));
        for (List<Point> values : from_station.values()) {
            values.sort(distanceFrom(station));
            for (int i = 0; i < values.size(); i++) {
                pq.add(new RankedPoint(values.get(i), i));
            }
        }

        int counter = 0;
        while (!pq.isEmpty()) {
            RankedPoint top = pq.poll();
            if (++counter == 200) {
                System.out.println(top.point().x() * 100 + top.point().y());
                break;
            }
        }
    }
}
