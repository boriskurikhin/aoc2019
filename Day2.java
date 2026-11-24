import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Day2 {
    public static void main(String[] args) throws IOException {
        File file = new File("./2.txt");
        FileReader reader = new FileReader(file);
        String seq = reader.readAllLines().get(0);

        Computer computer = new Computer(seq);
        outer:
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                computer.init(new HashMap(Map.of(1, 1L * i, 2, 1L * j)));
                computer.run();
                if (computer.read(0) == 19690720) {
                    System.out.println(100 * i + j);
                    break outer;
                }
            }
        }
    }
}
