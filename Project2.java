/*  Name: Kris Sy COMP482 Project 2
 *
 *  Loopyness = sum of squared differences between adjacent heights
 *  Goal: greedy alg to maximize loopyness (hn - h1)^2 + Sum(hi+1 - hi))^2 
 *  
 *  Approach?
 *  1. Sort heights: largest, smallest, next largest, next smallest, etc. to form arrangement with high loopyness
 *  2. Improve pair swaps locally. Each swap is accepted only if it increases the loopyness.
 */
import java.io.*;
import java.util.*;

public class Project2 {
    public static void main(String[] args) throws Exception {
        
        File inFile = new File("input.txt");
        
        try (Scanner sc = new Scanner(inFile)) {
            
            if (!sc.hasNextInt()) return;
            
            int n = sc.nextInt();
            long[] a = new long[n];

            for (int i = 0; i < n; i++) {
                if (!sc.hasNextLong()) throw new IllegalArgumentException("Not enough heights.");
                a[i] = sc.nextLong();
            }

            // Stage 1: build high–low alternating arrangement
            long[] start = highLowAlternating(a);

            // Stage 2: greedy hill-climb by pair swaps using O(1) delta scoring
            long[] best = start;
            long bestScore = loopyness(best);

            boolean improved = true;
            while (improved) {
                improved = false;
                
                // Try all unordered pairs; accept first improvement found 
                
                for (int i = 0; i < n; i++) {
                    for (int j = i + 1; j < n; j++) {
                        long gain = swapGain(best, bestScore, i, j);
                        if (gain > 0) {
                            // swap and update 
                            long tmp = best[i];
                            best[i] = best[j];
                            best[j] = tmp;
                            bestScore += gain;
                            improved = true;
                        }
                    }
                }
            }

            System.out.println(bestScore);
        }
    }

    // Build a strong initial arrangement by alternating largest, smallest, ...
    private static long[] highLowAlternating(long[] arr) {
        long[] sorted = arr.clone();
        Arrays.sort(sorted);
        int i = 0, j = sorted.length - 1, k = 0;
        long[] res = new long[sorted.length];
        boolean takeHigh = true;
        
        while (i <= j) {

            if (takeHigh) {
               
                res[k++] = sorted[j--];

            } else {
                
                res[k++] = sorted[i++];
            }
            takeHigh = !takeHigh;
        }
        return res;
    }

    // Compute loopyness 
    private static long loopyness(long[] h) {
        
        long s = 0L;
        int n = h.length;
        
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            long d = h[j] - h[i];
            s += d * d;
        }
        return s;

    }

    // Compute the gain (delta) in loopyness if i and j are swapped (i < j).
    private static long swapGain(long[] h, long currentScore, int i, int j) {
        int n = h.length;
        if (i == j) return 0;

        // Helper to get (u -> v) edge contribution
        java.util.function.BiFunction<Integer, Integer, Long> edge = (u, v) -> {
            long d = h[v] - h[u];
            return d * d;
        };

        // Indices around i and j (mod n)
        int im1 = (i - 1 + n) % n, ip1 = (i + 1) % n;
        int jm1 = (j - 1 + n) % n, jp1 = (j + 1) % n;

        // avoid double counting same edge
        long before = 0;
        
        // for i edges
        if (im1 != j) before += edge.apply(im1, i);
        if (ip1 != j) before += edge.apply(i, ip1);

        // For j edges
        if (jm1 != i) before += edge.apply(jm1, j);
        if (jp1 != i) before += edge.apply(j, jp1);

        // Values before swap
        long vi = h[i], vj = h[j];

        // After-swap contributions (treat i as holding vj, j as holding vi)
        long after = 0;

        // Edges around i become (im1 -> i[vj]) and (i[vj] -> ip1)
        if (im1 != j) after += sq(h[im1], vj);
        if (ip1 != j) after += sq(vj, h[ip1]);

        // Edges around j become (jm1 -> j[vi]) and (j[vi] -> jp1)
        if (jm1 != i) after += sq(h[jm1], vi);
        if (jp1 != i) after += sq(vi, h[jp1]);

        // Special handling for edge case if i and j share an edge:
        //  j == i+1 or i == j+1 (mod n) -- In those cases exclude the shared edge above
        boolean adjacent = (ip1 == j) || (jp1 == i);
        if (adjacent) {
            
            // shared edge before swap
            if (ip1 == j) {
                
                // before: (i -> j) = (h[j] - h[i])^2 ; after: (i[vj] -> j[vi]) = (vi - vj)^2
                long b = sq(h[i], h[j]);
                long a = sq(vj, vi);
                before += b;
                after += a;

            } else {
                
                // jp1 == i, before: (j -> i), after: (j[vi] -> i[vj])
                long b = sq(h[j], h[i]);
                long a = sq(vi, vj);
                before += b;
                after += a;

            }
        }
        return after - before; // positive => improvement
    }

    private static long sq(long u, long v) {
        long d = v - u;
        return d * d;
    }
}

