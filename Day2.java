import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Day2 {
    public static void main(String[] args) throws IOException {
        File file = new File("./2.txt");
        FileReader reader = new FileReader(file);
        String seq = reader.readAllLines().get(0);

        Computer computer = new Computer(seq);
        outer:
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                computer.run(i, j);
                if (computer.read(0) == 19690720) {
                    System.out.println(100 * i + j);
                    break outer;
                }
            }
        }
    }
}
