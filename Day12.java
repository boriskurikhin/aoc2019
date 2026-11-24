import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

record AxisState (int s1, int s2, int s3, int s4, int ds1, int ds2, int ds3, int ds4) {}

class Moon {
    int x, y, z, dx, dy, dz;

    public Moon(int sx, int sy, int sz) {
        x = sx;
        y = sy;
        z = sz;

        dx = 0;
        dy = 0;
        dz = 0;
    }

    public void applyGravity(Moon other) {
        if (other == this)
            return;

        dx += Integer.compare(other.x, x);
        dy += Integer.compare(other.y, y);
        dz += Integer.compare(other.z, z);
    }

    public void applyVelocity() {
        x += dx; 
        y += dy;
        z += dz;
    }

    public long energy() {
        long pos = Math.abs(1L * x) + Math.abs(1L * y) + Math.abs(1L * z);
        long vel = Math.abs(1L * dx) + Math.abs(1L * dy) + Math.abs(1L * dz);
        return pos * vel;
    }
}

public class Day12 {
    public static Map<Long, Integer> factorize(long num) {
        Map<Long, Integer> counts = new HashMap<>();

        for (long i = 2; i * i <= num; i++) {
            while (num % i == 0) {
                counts.merge(i, 1, Integer::sum);
                num /= i;
            }
        }

        if (num > 1)
            counts.merge(num, 1, Integer::sum);

        return counts;
    }

    public static void main(String[] args) {
        Moon[] moons = {
            new Moon(5, -1, 5),
            new Moon(0, -14, 2),
            new Moon(16, 4, 0),
            new Moon(18, 1, 16)
        };
        AxisState[] initial = {
            new AxisState(moons[0].x, moons[1].x, moons[2].x, moons[3].x, 0, 0, 0, 0),
            new AxisState(moons[0].y, moons[1].y, moons[2].y, moons[3].y, 0, 0, 0, 0),
            new AxisState(moons[0].z, moons[1].z, moons[2].z, moons[3].z, 0, 0, 0, 0)
        };

        Map<Long, Integer>[] factors = new HashMap[3];
        Long[] steps = new Long[3];
        
        for (int i = 0; i < 3; i++) {
            factors[i] = null;
            steps[i] = null;
        }

        for (long t = 1; ; t++) {
            if (Arrays.asList(steps).stream().allMatch(Objects::nonNull))
                break;

            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    moons[i].applyGravity(moons[j]);
                }
            }

            for (int i = 0; i < 4; i++) {
                moons[i].applyVelocity();
            }

            AxisState x = new AxisState(
                moons[0].x, moons[1].x, moons[2].x, moons[3].x,
                moons[0].dx, moons[1].dx, moons[2].dx, moons[3].dx);
            AxisState y = new AxisState(
                moons[0].y, moons[1].y, moons[2].y, moons[3].y,
                moons[0].dy, moons[1].dy, moons[2].dy, moons[3].dy);
            AxisState z = new AxisState(
                moons[0].z, moons[1].z, moons[2].z, moons[3].z,
                moons[0].dz, moons[1].dz, moons[2].dz, moons[3].dz);

            if (steps[0] == null && initial[0].equals(x))
                steps[0] = t;
            if (steps[1] == null && initial[1].equals(y))
                steps[1] = t;
            if (steps[2] == null && initial[2].equals(z))
                steps[2] = t;
        }

        for (int i = 0; i < 3; i++)
            factors[i] = factorize(steps[i]);

        //forgot how to calc LCM
        Map<Long, Integer> lcm = new HashMap<>();
        for (Map<Long, Integer> factor_map : factors) {
            for (Map.Entry<Long, Integer> entry : factor_map.entrySet()) {
                lcm.merge(entry.getKey(), entry.getValue(), Math::max);
            }
        }

        long result = lcm.entrySet()
            .stream()
            .mapToLong(entry -> Math.powExact(entry.getKey(), entry.getValue()))
            .reduce(1L, Math::multiplyExact);

        System.out.println(result);
    }
}
