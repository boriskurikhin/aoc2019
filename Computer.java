import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;


enum Mode { POSITIONAL, IMMEDIATE }
record Argument (int value, Mode mode) {}
record Instruction(int code, List<Argument> args ) {}

public class Computer implements Runnable {
    private final int size;
    private final int[] init;
    private int[] memory;
    private Scanner scanner;

    public List<Integer> input_history, output_history;
    public BlockingQueue<Integer> input_queue, output_queue;
    private final Map<Integer, Integer> arg_count = new HashMap<>(Map.of(
        1, 3, 
        2, 3, 
        3, 1, 
        4, 1, 
        5, 2,
        6, 2,
        7, 3,
        8, 3,
        99, 0
    ));

    public Computer(String instruction) {
        String[] tokens = instruction.split(",");

        size = tokens.length;
        init = new int[size];
        memory = new int[size];

        input_history = new ArrayList<>();
        output_history = new ArrayList<>();

        for (int i = 0; i < size; i++)
            init[i] = Integer.parseInt(tokens[i]);
    
        Arrays.fill(memory, 0);
    }

    public Computer(String instruction, Scanner scanner) {
        this(instruction);
        this.scanner = scanner;
    }

    public Computer(String instruction, BlockingQueue input_queue, BlockingQueue output_queue) {
        this(instruction);
        this.input_queue = input_queue;
        this.output_queue = output_queue;
    }

    public void run() {
        memory = Arrays.copyOf(init, size);
        int ip = 0;

        try {
            do { ip = run_command(ip); } while (ip != -1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run(int noun, int verb) {
        memory = Arrays.copyOf(init, size);
        memory[1] = noun;
        memory[2] = verb;
        int ip = 0;

        try {
            do { ip = run_command(ip); } while (ip != -1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int read(int addr) {
        return memory[addr];
    }

    private int resolve(Argument arg) {
        return arg.mode() == Mode.POSITIONAL ? memory[arg.value()] : arg.value();
    }

    private void write (Argument arg, int value) {
        if (arg.mode() != Mode.POSITIONAL)
            throw new RuntimeException("cant have immediate address!");
        memory[arg.value()] = value;
    }

    private int _multiply(Instruction inst, int ip) {
        Argument arg1 = inst.args().get(0),
                 arg2 = inst.args().get(1),
                 arg3 = inst.args().get(2);

        int val1 = resolve(arg1), val2 = resolve(arg2);
        write(arg3, val1 * val2);
        return ip + 1 + inst.args().size();
    }

    private int _add(Instruction inst, int ip) {
        Argument arg1 = inst.args().get(0),
                 arg2 = inst.args().get(1),
                 arg3 = inst.args().get(2);

        int val1 = resolve(arg1), val2 = resolve(arg2);
        write(arg3, val1 + val2);
        return ip + 1 + inst.args().size();
    }

    private int _in(Instruction inst, int ip) throws InterruptedException {
        Argument arg1 = inst.args().get(0);
        int num = 0;

        if (input_queue instanceof BlockingQueue) {
            num = input_queue.take();
        } else if (scanner instanceof Scanner) {
            num = scanner.nextInt(); 
        } else {
            throw new RuntimeException("input not configured!");
        }

        input_history.add(num);
        write(arg1, num);
        return ip + 1 + inst.args().size();
    }

    private int _out(Instruction inst, int ip) throws InterruptedException {
        Argument arg1 = inst.args().get(0);
        int val1 = resolve(arg1);

        if (output_queue instanceof BlockingQueue) {
            output_queue.put(val1);
        } else {
            System.out.println(val1);
        }

        output_history.add(val1);
        return ip + 1 + inst.args().size();
    }

    private int _jump(Instruction inst, int ip, boolean zero) {
        Argument arg1 = inst.args().get(0),
                 arg2 = inst.args().get(1);
        int val1 = resolve(arg1), val2 = resolve(arg2);

        if (!zero && val1 != 0)
            return val2;
        if (zero && val1 == 0)
            return val2;

        return ip + 1 + inst.args().size();
    }

    private int _le(Instruction inst, int ip) {
        Argument arg1 = inst.args().get(0),
                 arg2 = inst.args().get(1),
                 arg3 = inst.args().get(2);
        int val1 = resolve(arg1), val2 = resolve(arg2);
        int val3 = val1 < val2 ? 1 : 0;

        write(arg3, val3);
        return ip + 1 + inst.args().size();
    }

    private int _eq(Instruction inst, int ip) {
        Argument arg1 = inst.args().get(0),
                 arg2 = inst.args().get(1),
                 arg3 = inst.args().get(2);
        int val1 = resolve(arg1), val2 = resolve(arg2);
        int val3 = val1 == val2 ? 1 : 0;

        write(arg3, val3);
        return ip + 1 + inst.args().size();    
    }

    private Instruction read_command(int ip) {
        int code = memory[ip] % 100;
        int mode = memory[ip] / 100;
        int num_args = arg_count.get(code);
        List<Argument> args = new ArrayList<>();

        for (int i = 0; i < num_args; i++) {
            Mode m = mode % 10 == 0 
                ? Mode.POSITIONAL 
                : Mode.IMMEDIATE;
            args.add(new Argument(memory[ip + 1 + i], m));
            mode /= 10;
        }

        return new Instruction(code, args);
    }

    private int run_command (int ip) throws InterruptedException {
        Instruction inst = read_command(ip);
        return switch (inst.code()) {
            case 1 -> _add(inst, ip);
            case 2 -> _multiply(inst, ip);
            case 3 -> _in(inst, ip);
            case 5 -> _jump(inst, ip, false );
            case 6 -> _jump(inst, ip, true);
            case 7 -> _le(inst, ip);
            case 8 -> _eq(inst, ip);
            case 4 -> _out(inst, ip);
            case 99 -> -1;
            default -> throw new RuntimeException("invalid opcode");
        };
    }
}
