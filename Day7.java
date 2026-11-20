import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Day7 {

    public static int runConfiguration(int start_signal, int[] phase_settings, String program) throws InterruptedException {
        int n = phase_settings.length;
        Computer[] computers = new Computer[n];
        Thread[] threads = new Thread[n];

        BlockingQueue<Integer> initial_input = new LinkedBlockingQueue<>();
        initial_input.put(phase_settings[0]);
        initial_input.put(start_signal);

        BlockingQueue<Integer> input_queue = null;

        for (int i = 0; i < n; i++) {
            if (input_queue == null)
                input_queue = initial_input;

            BlockingQueue<Integer> output_queue = null;

            if (i + 1 < n) {
                output_queue = new LinkedBlockingQueue<>();
                output_queue.put(phase_settings[i + 1]);
            } else {
                output_queue = initial_input;
            }

            computers[i] = new Computer(program, input_queue, output_queue);
            threads[i] = new Thread(computers[i]);
            threads[i].start();

            input_queue = output_queue;
        }

        for (Thread t : threads)
            t.join();

        return computers[n - 1].output_history.getLast();
    }

    public static int permute (int offs, int[] setting, boolean[] used, int i, String program) throws InterruptedException{
        if (i >= setting.length)
            return runConfiguration(0, setting, program);
        int best = 0;
        for (int j = offs; j < offs + setting.length; j++) {
            if (!used[j - offs]) {
                used[j - offs] = true;
                setting[i] = j;
                best = Math.max(
                    best,
                    permute(offs, setting, used, i + 1, program)
                );
                setting[i] = -1;
                used[j - offs] = false;
            }
        }
        return best;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        File file = new File("./7.txt");
        FileReader reader = new FileReader(file);
        String seq = reader.readAllLines().get(0);

        int[] setting = new int[5];
        boolean[] used = new boolean[5];

        Arrays.fill(setting, -1);
        Arrays.fill(used, false);

        int best = permute(5, setting, used, 0, seq);
        System.out.println(best);
    }
}
