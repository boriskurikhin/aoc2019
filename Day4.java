public class Day4 {

    public static int check(String s, int lo, int hi) {
        int n = Integer.parseInt(s), l = s.length();
        boolean found = false;

        for (int i = 0; i < l; i++) {
            if (i + 1 < l && s.charAt(i) > s.charAt(i + 1))
                return 0;
            if (i + 1 < l && s.charAt(i) == s.charAt(i + 1)) {
                if (i - 1 >= 0 && s.charAt(i) == s.charAt(i - 1))
                    continue;
                if (i + 2 < l && s.charAt(i) == s.charAt(i + 2))
                    continue;
                found = true;
            }
        } 

        return (found && n >= lo && n <= hi) ? 1 : 0;
    }

    public static int count(String s, int lo, int hi) {
        if (s.length() > 6)
            return 0;
        if (s.length() == 6)
            return check(s, lo, hi);
        int acc = 0;
        char st = s.isEmpty() ? '1' : '0';
        for (char c = st; c <= '9'; c++) {
            acc += count(s + c, lo, hi);
        }
        return acc;
    }
    public static void main(String[] args) {
        int low = 168630;
        int high = 718098;
        System.out.println(count("", low, high));
    }
}
