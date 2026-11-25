import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Day15 {
    enum Tile { WALL, EMPTY, FLAG }
    record Point (int y, int x) {}
    record State (ComputerState computer_state, Point coord, long output, int steps) {}

    public static Tile getTile (long status) {
        return switch((int) status) {
            case 0 -> Tile.WALL;
            case 1 -> Tile.EMPTY;
            case 2 -> Tile.FLAG;
            default -> throw new RuntimeException("Invalid flag!");
        };
    }

    public static void main(String[] args) throws IOException, InterruptedException{
        File file = new File("./15.txt");
        FileReader reader = new FileReader(file);
        List<String> lines = reader.readAllLines();
        String program = lines.get(0);

        Computer throwaway = new Computer(program);
        throwaway.init(null);

        Map<Point, Tile> map = new HashMap<>();
        
        Queue<State> queue = new LinkedList<>();
        queue.add(new State(throwaway.save(), new Point(0, 0), 1L, 0));

        final int[][] deltas = {
            {1, 1, 0},
            {2, -1, 0},
            {3, 0, -1},
            {4, 0, 1}
        };

        Point start_point = null;

        while (!queue.isEmpty()) {
            State state = queue.poll();

            if (map.containsKey(state.coord()))
                continue;

            Tile tile = getTile(state.output());
            map.put(state.coord(), tile);

            if (tile == Tile.WALL)
                continue;

            if (tile == Tile.FLAG) {
                System.out.println("Dist to oxygen: " + state.steps());
                start_point = state.coord();
            }

            for (int i = 0; i < 4; i++) {
                int command = deltas[i][0];
                int dy = deltas[i][1];
                int dx = deltas[i][2];

                Point next = new Point(state.coord().y() + dy, state.coord().x() + dx);

                if (map.containsKey(next))
                    continue;

                BlockingQueue<Long> _in = new LinkedBlockingQueue<>(),
                                    _out = new LinkedBlockingQueue<>();

                Computer comp = new Computer(program, _in, _out);
                comp.restore(state.computer_state());
                _in.add(1L * command);

                Long result = comp.runUntilOutput();
                queue.add(new State(
                    comp.save(),
                    next,
                    result,
                    state.steps() + 1  
                ));
            }
        }

        Set<Point> visited = new HashSet<>(Set.of(start_point)), visited2 = null;
        Queue<Point> queue2 = new LinkedList<>(List.of(start_point)), queue3 = null;
        int time = 0;

        while (!queue2.isEmpty()) {
            queue3 = new LinkedList<>();
            visited2 = new HashSet<>(Set.copyOf(visited));

            while (!queue2.isEmpty()) {
                Point coord = queue2.poll();

                for (int i = 0; i < 4; i++) {
                    int dy = deltas[i][1], dx = deltas[i][2];
                    Point next = new Point(coord.y() + dy, coord.x() + dx);

                    if (visited2.contains(next))
                        continue;

                    if (map.get(next) == Tile.WALL)
                        continue;

                    visited2.add(next);
                    queue3.add(next);
                }
            }

            if (queue3.isEmpty()) {
                System.out.println("Time to fill: " + time);
                break;
            }

            queue2 = queue3;
            visited = visited2;

            ++time;
        }
    }
}
