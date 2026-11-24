import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFrame;

public class Day13 extends JFrame implements Runnable {
    record Point (long y, long x ) {}

    private Point ball, paddle;
    private int width, height;
    private static final int scale = 16;
    private BufferedImage image;
    private int[] pixels;
    private BlockingQueue<Long> in_queue, out_queue;
    
    private static Map<Point, Long> screen;
    private Thread computer_thread;
    private boolean running;

    private Dimension getDimension(String program) throws InterruptedException {
        BlockingQueue<Long> out_queue = new LinkedBlockingQueue<>();

        Computer computer = new Computer(program, null, out_queue);
        computer.init(null);
        computer.run();

        Map<Point, Long> seen = new HashMap<>();
        while (out_queue.size() > 1) {
            long x = out_queue.take(), y = out_queue.take(), tile_id = out_queue.take();
            seen.put(new Point(y, x), tile_id);
        }

        if (out_queue.take() != Computer.HALT)
            throw new RuntimeException("Invlaid final value!");

        for (Entry<Point, Long> entry : seen.entrySet()) {
            if (entry.getValue() == 3L)
                paddle = entry.getKey();

            if (entry.getValue() == 4L)
                ball = entry.getKey();
        }

        // System.out.println(seen.values().stream().filter(v -> v == 2L).count());
        LongSummaryStatistics 
            xstat = seen.keySet().stream().mapToLong(Point::x).summaryStatistics(),
            ystat = seen.keySet().stream().mapToLong(Point::y).summaryStatistics();

        int width = (int) xstat.getMax() - (int) xstat.getMin() + 1;
        int height = (int) ystat.getMax() - (int) ystat.getMin() + 1;

        return new Dimension(width, height);
    }

    public Day13(String program) throws InterruptedException {
        Dimension dimension = getDimension(program);

        running = false;
        width = (int) dimension.getWidth() * scale;
        height = (int) dimension.getHeight() * scale;

        in_queue = new LinkedBlockingQueue<>();
        out_queue = new LinkedBlockingQueue<>();

        screen = new HashMap<>();
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) (image.getRaster().getDataBuffer())).getData();

        getContentPane().setPreferredSize(new Dimension(width, height));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // addKeyListener(new KeyAdapter() {
        //     @Override 
        //     public void keyPressed(KeyEvent e) {
        //         switch (e.getKeyCode()) {
        //             case KeyEvent.VK_LEFT -> in_queue.add(-1L);
        //             case KeyEvent.VK_RIGHT -> in_queue.add(1L);
        //             default -> in_queue.add(0L);
        //         }
        //     }
        // });

        Computer computer = new Computer(program, in_queue, out_queue);
        computer.init(new HashMap<>(Map.of(0, 2L)));
        computer_thread = new Thread(computer);
        computer_thread.start();
    }

    @Override 
    public synchronized void paint(Graphics g) {
        g.drawImage(image, getInsets().left, getInsets().top, null);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        File file = new File("./13.txt");
        FileReader reader = new FileReader(file);
        List<String> lines = reader.readAllLines();

        String program = lines.get(0);

        Thread mainThread = new Thread(new Day13(program));
        mainThread.start();
        mainThread.join();
    }

    public synchronized void render() {
        for (Entry<Point, Long> entry : screen.entrySet()) {
            int x = (int) entry.getKey().x();
            int y = (int) entry.getKey().y();
            long tile = entry.getValue();

            int col = switch ((int) tile) {
                case 0 -> 0xFFFFFFFF;
                case 1 -> 0xFF000000;
                case 2 -> 0xFFFF0000;
                case 3 -> 0xFF00FF00;
                case 4 -> 0xFF0000AA;
                default -> throw new RuntimeException("Invalid tile");
            };

            for (int yy = 0; yy < scale; yy++) {
                for (int xx = 0; xx < scale; xx++) {
                    int yp = y * scale + yy;
                    int xp = x * scale + xx;
                    pixels[yp * width + xp] = col;
                }
            }
        }
    }

    public void run() {
        running = true;
        while (running) {
            tick();
            render();
            repaint();

            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void tick() {
        while (out_queue.size() >= 3) {
            long x = out_queue.poll();
            long y = out_queue.poll();
            long tile = out_queue.poll();

            if (x == -1L && y == 0L) {
                System.out.println("Score: " + tile);
                continue;
            }

            if (tile == 3L)
                paddle = new Point(y, x);

            if (tile == 4L)
                ball = new Point(y, x);

            screen.put(new Point(y, x), tile);
        }
        
        in_queue.add(1L * Long.compare(ball.x(), paddle.x()));

        if (Objects.equals(out_queue.peek(), Computer.HALT)) {
            running = false;
        }
    }
}
