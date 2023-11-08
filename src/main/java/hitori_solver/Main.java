package hitori_solver;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        int[][] inputMatrix = readInput("src/main/java/hitori_solver/input/input.txt");
        HitoriSolver a = new HitoriSolver(inputMatrix.length, inputMatrix);
        a.Solve();
    }

    public static int[][] readInput(String path) {
        int matrixSize;
        try {
            File file = new File(path);
            Scanner scanner = new Scanner(file);
            String firstElement = scanner.next();
            matrixSize = Integer.parseInt(firstElement);

            int[][] array = new int[matrixSize][matrixSize];
            int num = 0;
            while (scanner.hasNext()) {
                String element = scanner.next();
                if (element.equals(".")) array[num / matrixSize][num % matrixSize] = 0;
                else array[num / matrixSize][num % matrixSize] = Integer.parseInt(element);
                num++;
            }
            scanner.close();

            return array;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new int[0][0];
    }
}