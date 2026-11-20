import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Day8 extends JFrame {
    BufferedImage image;
    String pic;
    final int raw_width, raw_height, width, height, scale = 32;
    final int T = 0x2200FF00, W = 0xFFFFFFFF, B = 0xFF000000; 
    int[] image_pixels;

    public Day8(int width, int height, String pic) {
        this.pic = pic;
        raw_width = width;
        raw_height = height;
        this.width = width * scale;
        this.height = height * scale;

        image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        image_pixels = ((DataBufferInt) (image.getRaster().getDataBuffer())).getData();

        int[] pixels = get_pixels();
        for (int i = 0; i < pixels.length; i++) {
            int y = i / raw_width, x = i % raw_width;
            int col = switch (pixels[i]) {
                case 2 -> T;
                case 1 -> W;
                case 0 -> B;
                default -> throw new RuntimeException("Invalid color");
            };
            for (int yy = 0; yy < scale; yy++) {
                for (int xx = 0; xx < scale; xx++) {
                    int yp = y * scale + yy, xp = x * scale + xx;
                    image_pixels[yp * this.width + xp] = col;
                }
            }
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        Insets insets = getInsets();
        setSize(this.width + insets.left + insets.right, this.height + insets.top + insets.bottom);
        setLocationRelativeTo(null);
        
    }

    public int[] get_pixels() {
        int[] pixels = new int[raw_width * raw_height];
        Arrays.fill(pixels, 2);

        int pix = 0;
        while (pix < pic.length()) {
            for (int i = 0; i < raw_width * raw_height; i++) {
                int p = pic.charAt(pix++) - '0';
                if (pixels[i] == 2)
                    pixels[i] = p;
            }
        }

        return pixels;
    }

    public static void main(String[] args) throws IOException {
        File file = new File("./8.txt");
        FileReader reader = new FileReader(file);
        String seq = reader.readAllLines().get(0);

        int width = 25, height = 6;
        SwingUtilities.invokeLater(() -> new Day8(width, height, seq));
    }

    @Override 
    public void paint(Graphics g) {
        g.drawImage(image, getInsets().left, getInsets().top, null);
    }
}