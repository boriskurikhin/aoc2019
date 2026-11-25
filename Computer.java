import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

enum Mode { POSITIONAL, IMMEDIATE, RELATIVE }
record Argument (long value, Mode mode) {}
record Instruction(int code, List<Argument> args ) {}

public class Computer implements Runnable {
    private static final int size = 10_000;
    private final long[] init;
    private long[] memory;
    private int ip;
    private Scanner scanner;

    public static final long HALT = Long.MIN_VALUE;

    private int rbase;
    public List<Long> input_history, output_history;
    public BlockingQueue<Long> input_queue, output_queue;
    private final Map<Integer, Integer> arg_count = new HashMap<>(Map.of(
        1, 3, 
        2, 3, 
        3, 1, 
        4, 1, 
        5, 2,
        6, 2,
        7, 3,
        8, 3,
        9, 1,
        99, 0
    ));

    public Computer(String instruction) {
        String[] tokens = instruction.split(",");
        if (tokens.length > size)
            throw new RuntimeException("Need to increase size!");

        init = new long[size];
        memory = new long[size];
        rbase = 0;

        input_history = new ArrayList<>();
        output_history = new ArrayList<>();

        for (int i = 0; i < tokens.length; i++)
            init[i] = Long.parseLong(tokens[i]);
    
        Arrays.fill(memory, 0L);
    }

    public Computer(String instruction, Scanner scanner) {
        this(instruction);
        this.scanner = scanner;
    }

    public Computer(
        String instruction,
        BlockingQueue<Long> input_queue,
        BlockingQueue<Long> output_queue
    ) {
        this(instruction);
        this.input_queue = input_queue;
        this.output_queue = output_queue;
    }

    public void init(Map<Integer, Long> values) {
        ip = rbase = 0;
        memory = Arrays.copyOf(init, size);
        
        if (values != null) {
            for (Entry<Integer, Long> entry : values.entrySet()) {
                memory[entry.getKey()] = entry.getValue();
            }
        }
    }

    public ComputerState save () {
        return new ComputerState(memory, ip, rbase);
    }

    public void restore(ComputerState state) {
        memory = state.memory().clone();
        ip = state.ip();
        rbase = state.rbase();
    }

    public void run() {
        try {
            do { ip = run_command(ip); } while (ip != -1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (output_queue != null)
                output_queue.add(HALT);
        }
    }

    public Long runUntilOutput() throws InterruptedException {
        if (output_queue == null)
            throw new RuntimeException("No output queue!");

        while (ip != -1 && output_queue.isEmpty()) {
            ip = run_command(ip);
        }

        return output_queue.poll();
    }

    public long read(int addr) {
        return memory[addr];
    }

    private long resolve(Argument arg) {
        return switch(arg.mode()) {
            case Mode.POSITIONAL -> memory[(int) arg.value()];
            case Mode.IMMEDIATE -> arg.value();
            case Mode.RELATIVE -> memory[rbase + (int) arg.value()];
            default -> throw new RuntimeException("Invalid mode for resolution");
        };
    }

    private void write (Argument arg, long value) {
        long addr = switch(arg.mode()) {
            case Mode.POSITIONAL -> arg.value();
            case Mode.RELATIVE -> rbase + arg.value();
            default -> throw new RuntimeException("Cannot write in " + arg.mode().name() + "!");
        };

        if (addr < 0L || addr >= 1L * size)
            throw new RuntimeException("Invalid address!");

        memory[(int) addr] = value;
    }

    private int _adjust_rbase(Instruction inst, int ip) {
        Argument arg1 = inst.args().get(0);

        long val1 = resolve(arg1);
        rbase += (int) val1;
        return ip + 1 + inst.args().size();
    }

    private int _multiply(Instruction inst, int ip) {
        Argument arg1 = inst.args().get(0),
                 arg2 = inst.args().get(1),
                 arg3 = inst.args().get(2);

        long val1 = resolve(arg1), val2 = resolve(arg2);
        write(arg3, val1 * val2);
        return ip + 1 + inst.args().size();
    }

    private int _add(Instruction inst, int ip) {
        Argument arg1 = inst.args().get(0),
                 arg2 = inst.args().get(1),
                 arg3 = inst.args().get(2);

        long val1 = resolve(arg1), val2 = resolve(arg2);
        write(arg3, val1 + val2);
        return ip + 1 + inst.args().size();
    }

    private int _in(Instruction inst, int ip) throws InterruptedException {
        Argument arg1 = inst.args().get(0);
        long num = 0;

        if (input_queue != null) {
            num = input_queue.take();
        } else if (scanner != null) {
            num = scanner.nextLong(); 
        } else {
            throw new RuntimeException("input not configured!");
        }

        input_history.add(num);
        write(arg1, num);
        return ip + 1 + inst.args().size();
    }

    private int _out(Instruction inst, int ip) throws InterruptedException {
        Argument arg1 = inst.args().get(0);
        long val1 = resolve(arg1);

        if (output_queue != null) {
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
        long val1 = resolve(arg1), val2 = resolve(arg2);

        if (!zero && val1 != 0L)
            return (int) val2;
        if (zero && val1 == 0)
            return (int) val2;

        return ip + 1 + inst.args().size();
    }

    private int _le(Instruction inst, int ip) {
        Argument arg1 = inst.args().get(0),
                 arg2 = inst.args().get(1),
                 arg3 = inst.args().get(2);
        long val1 = resolve(arg1), val2 = resolve(arg2);
        long val3 = val1 < val2 ? 1L : 0L;

        write(arg3, val3);
        return ip + 1 + inst.args().size();
    }

    private int _eq(Instruction inst, int ip) {
        Argument arg1 = inst.args().get(0),
                 arg2 = inst.args().get(1),
                 arg3 = inst.args().get(2);
        long val1 = resolve(arg1), val2 = resolve(arg2);
        long val3 = val1 == val2 ? 1L : 0L;

        write(arg3, val3);
        return ip + 1 + inst.args().size();    
    }

    private Instruction read_command(int ip) {
        int code = (int) memory[ip] % 100;
        int mode = (int) memory[ip] / 100;
        int num_args = arg_count.get(code);
        List<Argument> args = new ArrayList<>();

        for (int i = 0; i < num_args; i++) {
            Mode m = switch(mode % 10) {
                case 0 -> Mode.POSITIONAL;
                case 1 -> Mode.IMMEDIATE;
                case 2 -> Mode.RELATIVE;
                default -> throw new RuntimeException("invalid mode");
            };
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
            case 4 -> _out(inst, ip);
            case 5 -> _jump(inst, ip, false );
            case 6 -> _jump(inst, ip, true);
            case 7 -> _le(inst, ip);
            case 8 -> _eq(inst, ip);
            case 9 -> _adjust_rbase(inst, ip);
            case 99 -> -1;
            default -> throw new RuntimeException("invalid opcode");
        };
    }
}
