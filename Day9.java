import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Day9 {
    public static void main(String[] args) throws IOException {
        File file = new File("./9.txt");
        FileReader reader = new FileReader(file);
        String seq = reader.readAllLines().get(0);
        Scanner scan = new Scanner(System.in);

        Computer computer = new Computer(seq, scan);
        computer.init(null);
        computer.run();
    }
}
