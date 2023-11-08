package hitori_solver;

public class Main {
    public static void main(String[] args) {
        int[][] input = new int[][] {
            {3, 3, 4, 5, 5},
            {1, 4, 3, 2, 4},
            {4, 1, 5, 3, 1},
            {1, 4, 5, 1, 5},
            {5, 2, 1, 4, 1}
        };
        HitoriSolver a = new HitoriSolver(5, input);
        a.Solve();
    }
}