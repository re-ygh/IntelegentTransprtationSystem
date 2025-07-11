import java.util.*;

//

public class TSPSolver {
    private final int m; // تعداد دانشگاه‌ها
    private final double[][] cost; // ماتریس هزینه بین دانشگاه‌ها
    private final double[][] dp; // جدول برنامه‌نویسی پویا
    private final int[][] parent; // مسیر قبلی برای بازیابی مسیر

    public TSPSolver(double[][] cost) {
        this.m = cost.length;
        this.cost = cost;
        int maxMask = 1 << m;
        this.dp = new double[maxMask][m];
        this.parent = new int[maxMask][m];

        for (int mask = 0; mask < maxMask; mask++) {
            Arrays.fill(dp[mask], Double.POSITIVE_INFINITY);
            Arrays.fill(parent[mask], -1);
        }
    }

    public void solve() {
        // مقداردهی اولیه: شروع از هر دانشگاه
        for (int i = 0; i < m; i++) {
            dp[1 << i][i] = 0;
        }

        int fullMask = (1 << m) - 1;

        // پر کردن جدول dp
        for (int mask = 0; mask <= fullMask; mask++) {
            for (int i = 0; i < m; i++) {
                if ((mask & (1 << i)) == 0) continue;
                double cur = dp[mask][i];
                if (cur == Double.POSITIVE_INFINITY) continue;

                for (int j = 0; j < m; j++) {
                    if ((mask & (1 << j)) != 0) continue;
                    int nextMask = mask | (1 << j);
                    double nd = cur + cost[i][j];

                    if (nd < dp[nextMask][j]) {
                        dp[nextMask][j] = nd;
                        parent[nextMask][j] = i;
                    }
                }
            }
        }
    }

    // بازیابی مسیر بهینه
    public List<Integer> getOptimalPath() {
        int fullMask = (1 << m) - 1;
        double best = Double.POSITIVE_INFINITY;
        int last = -1;

        for (int i = 0; i < m; i++) {
            if (dp[fullMask][i] < best) {
                best = dp[fullMask][i];
                last = i;
            }
        }

        List<Integer> path = new ArrayList<>();
        int mask = fullMask;
        while (last != -1) {
            path.add(last);
            int prev = parent[mask][last];
            mask ^= (1 << last);
            last = prev;
        }

        Collections.reverse(path);
        return path;
    }

    public double getOptimalCost() {
        int fullMask = (1 << m) - 1;
        double best = Double.POSITIVE_INFINITY;

        for (int i = 0; i < m; i++) {
            best = Math.min(best, dp[fullMask][i]);
        }

        return best;
    }
}
