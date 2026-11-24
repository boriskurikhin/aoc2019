import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;


enum Color { BLACK, WHITE }
record Point (int y, int x) {}

class Robot {
    final static int inf = 0x3f3f3f3f;
    int x, y, dx = 0, dy = 1;
    long top, bottom, left, right;
    BlockingQueue<Long> input_queue, output_queue;
    Map<Point, Color> known_points;
    Computer computer;

    public Robot(int sy, int sx, Computer computer, BlockingQueue<Long> input_queue, BlockingQueue<Long> output_queue) {
        y = sy;
        x = sx;
        this.computer = computer;
        this.input_queue = input_queue;
        this.output_queue = output_queue;
        known_points = new HashMap<>();
        top = -inf; bottom = inf; left = inf; right = -inf;
    }

    public boolean step() throws InterruptedException {
        Color col = known_points.getOrDefault(new Point(y, x), Color.BLACK);
        input_queue.add(col == Color.BLACK ? 0L : 1L);

        Long out = output_queue.take();
        if (out == Computer.HALT)
            return false;

        Color paintCol = out == 0L ? Color.BLACK : Color.WHITE;
        known_points.put(new Point(y, x), paintCol);

        left = Math.min(left, x);
        right = Math.max(right, x);
        top = Math.max(top, y);
        bottom = Math.min(bottom, y);

        Long rot = output_queue.take();
        int temp_dy = dy;

        if (rot == 0L) {
            dy = dx;
            dx = -temp_dy;
        } else if (rot == 1L) {
            dy = -dx;
            dx = temp_dy;
        } else 
            throw new RuntimeException("Invalid rotation!");

        y += dy;
        x += dx;

        return true;
    }
}

public class Day11 extends JFrame {
    BufferedImage image;
    int[] pixels;
    int width, height;
    private final int SCALE = 32;

    public Day11(int width, int height, Robot r) {
        this.width = width;
        this.height = height;

        int w = width * SCALE;
        int h = height * SCALE;

        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) (image.getRaster().getDataBuffer())).getData();

        for (Point p : r.known_points.keySet()) {
            Color c = r.known_points.get(p);
            int y = (int) r.top - p.y();
            int x = p.x() - (int) r.left;

            int col = c == Color.BLACK ? 0x000000 : 0xFFFFFF;
            for (int yy = 0; yy < SCALE; yy++) {
                for (int xx = 0; xx < SCALE; xx++) {
                    int yp = y * SCALE + yy;
                    int xp = x * SCALE + xx;
                    pixels[yp * w + xp] = col;
                }
            }
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        Insets insets = getInsets();
        setSize(w + insets.left + insets.right, h + insets.top + insets.bottom);
        setLocationRelativeTo(null);

    }

    @Override 
    public void paint(Graphics g) {
        g.drawImage(image, getInsets().left, getInsets().top, null);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        File file = new File("./11.txt");
        FileReader reader = new FileReader(file);
        List<String> lines = reader.readAllLines();

        BlockingQueue<Long> input_queue = new LinkedBlockingQueue<>(), 
                            output_queue = new LinkedBlockingQueue<>(); 

        Computer computer = new Computer(
            lines.get(0), 
            input_queue,
            output_queue
        );

        Thread computer_thread = new Thread(computer);
        computer_thread.start();

        boolean running = true;
        Robot r = new Robot(0, 0, computer, input_queue, output_queue);
        r.known_points.put(new Point(0, 0), Color.WHITE);

        do {
            running = r.step();
        } while (running);

        try {
            computer_thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long width = r.right - r.left + 1L;
        long height = r.top - r.bottom + 1L;

        SwingUtilities.invokeLater(() -> new Day11((int) width, (int) height, r));
    }
}
