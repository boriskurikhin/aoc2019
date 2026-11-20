import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class Day6 {

    static Map<String, Integer> cache;
    static int count_orbits(Map<String, String> orbits, String planet) {
        if (orbits.get(planet) == null)
            return 0;
        if (cache.containsKey(planet))
            return cache.get(planet);
        int ans = 1 + count_orbits(orbits, orbits.get(planet));
        cache.put(planet, ans);
        return ans;
    }

    record Point(String planet, int dist) {};

    public static void main(String[] args) throws IOException {
        File file = new File("./6.txt");
        FileReader reader = new FileReader(file);
        List<String> lines = reader.readAllLines();
        cache = new HashMap<>();

        Set<String> planets = new HashSet<>();
        Map<String, String> orbits = new HashMap<>();
        Map<String, List<String>> edges = new HashMap<>();

        for (String tok : lines) {
            String[] ln = tok.split("\\)");
            assert orbits.get(ln[1]) == null;
            orbits.put(ln[1], ln[0]);
            if (!edges.containsKey(ln[0]))
                edges.put(ln[0], new ArrayList<>());
            if (!edges.containsKey(ln[1]))
                edges.put(ln[1], new ArrayList<>());
            edges.get(ln[0]).add(ln[1]);
            edges.get(ln[1]).add(ln[0]);
            planets.add(ln[0]);
            planets.add(ln[1]);
        }

        // int total = 0;
        // for (String planet : planets) {
        //     total += count_orbits(orbits, planet);
        // }

        // System.out.println(total);

        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point("YOU", 0));
        while (!queue.isEmpty()) {
            Point top = queue.poll();
            if (top.planet().equals("SAN")) {
                System.out.println(top.dist() - 2);
                break;
            }
            List<String> orbit_edges = edges.get(top.planet());
            edges.put(top.planet(), new ArrayList<>());
            for (String e : orbit_edges) {
                queue.add(new Point(e, top.dist() + 1));
            }
        }
    }
}
