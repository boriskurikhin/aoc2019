import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Day3 {

    record Point(int x, int y) {
        public Point move (Point delta) {
            return new Point(x + delta.x, y + delta.y);
        }
    }

    public static Point deltas (char dir) {
        return switch (dir) {
            case 'U' -> new Point(0, 1);
            case 'D' -> new Point(0, -1);
            case 'L' -> new Point(-1, 0);
            case 'R' -> new Point(1, 0);
            default -> throw new IllegalArgumentException("Invalid direction: " + dir);
        };
    }

    public static void main(String[] args) throws IOException {
        File file = new File("./3.txt");
        FileReader reader = new FileReader(file);
        List<String> lines = reader.readAllLines();

        String first = lines.get(0), second = lines.get(1);
        Map<Point, Integer> steps = new HashMap<>(), steps2 = new HashMap<>();

        int step_count = 0, closest = Integer.MAX_VALUE;
        Point p = new Point(0, 0);

        for (String delta : first.split(",")) {
            char dir = delta.charAt(0);
            int amount = Integer.parseInt(delta.substring(1));
            for (int i = 0; i < amount; i++) {
                p = p.move(deltas(dir));
                ++step_count;
                if (!steps.containsKey(p))
                    steps.put(p, step_count);
            }
        }

        p = new Point(0, 0);
        step_count = 0;

        for (String delta : second.split(",")) {
            char dir = delta.charAt(0);
            int amount = Integer.parseInt(delta.substring(1));
            for (int i = 0; i < amount; i++) {
                ++step_count;
                p = p.move(deltas(dir));
                if (!steps2.containsKey(p))
                    steps2.put(p, step_count);
                if (steps.containsKey(p))
                    closest = Math.min(closest, steps.get(p) + steps2.get(p));
            }
        }

        System.out.println(closest);
    
    }
}
