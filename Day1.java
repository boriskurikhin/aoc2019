import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Day1 {

    static long calc (long mass) {
        long x = mass / 3L - 2L;
        if (x <= 0L)
            return 0L;
        return x + calc(x);
    }

    public static void main(String[] args) throws IOException {
        File file = new File("./1.txt");
        FileReader reader = new FileReader(file);
        long cur = 0L; 
        for (String line : reader.readAllLines()) {
            cur += calc(Long.parseLong(line));
        }
        System.out.println(cur);
    }
}