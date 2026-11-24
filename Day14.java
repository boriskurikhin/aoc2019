import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Day14 {

    record Item (String name, long amount) {}

    public static long countOre (long fuel, Map<Item, List<Item>> recipes ) {
        boolean reacted = false;
        Map<String, Long> 
            to_convert = new HashMap<>(Map.of("FUEL", fuel)),
            excess = new HashMap<>();

        do {
            reacted = false;

            for (Entry<Item, List<Item>> recipe : recipes.entrySet()) {
                String name = recipe.getKey().name();

                long amount = recipe.getKey().amount(),
                     need = to_convert.getOrDefault(name, 0L),
                     available = excess.getOrDefault(name, 0L);

                if (need <= 0L)
                    continue;

                if (available >= need) {
                    to_convert.remove(name);
                    excess.remove(name);

                    if (available > need) 
                        excess.put(name, available - need);

                    continue;
                }

                long remains = need - available,
                     multi = (remains + amount - 1) / amount;

                available += multi * amount;
                available -= need;

                excess.remove(name);
                to_convert.remove(name);

                if (available > 0) 
                    excess.put(name, available);

                for (Item item : recipe.getValue()) {
                    to_convert.merge(item.name(), multi * item.amount(), Long::sum);
                }

                reacted = true;
            }
        } while (reacted);

        return to_convert.getOrDefault("ORE", 0L);
    }

    public static void main(String[] args) throws IOException {
        File file = new File("./14.txt");
        FileReader reader = new FileReader(file);

        List<String> lines = reader.readAllLines();
        Map<Item, List<Item>> recipes = new HashMap<>();

        for (String line : lines) {
            List<Item> reqs = new ArrayList<>();
            String[] eq = line.split(" => ");

            for (String item : eq[0].split(",")) {
                String[] parts = item.trim().split(" ");
                reqs.add(new Item(parts[1], Long.parseLong(parts[0])));
            }

            String[] parts = eq[1].trim().split(" ");
            Item result = new Item(parts[1], Long.parseLong(parts[0]));
            recipes.put(result, reqs);
        }

        final long ore_target = 1000000000000L;
        long low = 1L, high = 10L * ore_target;

        while (low <= high) {
            long fuel = low + (high - low) / 2L;
            long ore = countOre(fuel, recipes);

            if (ore > ore_target)
                high = fuel - 1;
            else
                low = fuel + 1;
        }

        System.out.println(high);
    }
}
