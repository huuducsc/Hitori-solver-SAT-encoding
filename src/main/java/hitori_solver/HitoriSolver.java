package hitori_solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

public class HitoriSolver {
    public int matrixSize;
    public String status = "UNSAT";
    public long time = 0;
    public int[][] input;
    private Map<String, Integer> idMap;
    private int numberOfVariable = 0;
    private ArrayList<int[]> cnfClause;

    HitoriSolver(int matrixSize, int[][] input) {
        this.matrixSize = matrixSize;
        this.idMap = new HashMap<String, Integer>();
        this.input = input;
        this.cnfClause = new ArrayList<int[]>();
    }

    public void Solve() {
        InitIdMap();
        GenerateFirstRuleClause();
        // PrintClause();
        GenerateSecondRuleClause();
        //GenerateThirdRuleClause();
        try {
            SAT4JSolver();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }

    }

    private void InitIdMap() {
        // Make id for variable Wij which represent for "Is cell (i, j) black or white?"
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                String stringID = makeStringId("W", i, j, -1, -1);
                // System.out.println(stringID + " " +
                // Integer.toString(CreateOrGetNumberID(stringID)) + " ,");
                CreateOrGetNumberID(stringID);
            }
        }

        // System.out.println();
        // Make id for variable Cijkl which represent for "Is exist a "black path" from
        // cell (i, j) to cell (k, l)?"
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                for (int k = 0; k < matrixSize; k++) {
                    for (int l = 0; l < matrixSize; l++) {
                        String stringID = makeStringId("C", i, j, k, l);
                        // System.out.print(stringID + ",");
                        CreateOrGetNumberID(stringID);
                    }
                }
            }
        }
        System.out.println(numberOfVariable);
    }

    public void GenerateFirstRuleClause() {
        //At most one: each value exists most once in each row, each column
        for (int i = 0; i < matrixSize; i++) {
            Map<Integer, ArrayList<Integer>> duplicateMapColumn = new HashMap<Integer, ArrayList<Integer>>();
            Map<Integer, ArrayList<Integer>> duplicateMapRow = new HashMap<Integer, ArrayList<Integer>>();
            for (int j = 0; j < matrixSize; j++) {
                if (!duplicateMapColumn.containsKey(input[i][j])) {
                    duplicateMapColumn.put(input[i][j], new ArrayList<Integer>());
                }
                duplicateMapColumn.get(input[i][j]).add(j);
                if (!duplicateMapRow.containsKey(input[j][i])) {
                    duplicateMapRow.put(input[j][i], new ArrayList<Integer>());
                }
                duplicateMapRow.get(input[j][i]).add(j);
            }
            for (int duplicateValues : duplicateMapColumn.keySet()) {
                ArrayList<Integer> positionList = duplicateMapColumn.get(duplicateValues);
                if (positionList.size() > 1) {
                    // convolution combination 2 of number of duplicateValues
                    for (int pos = 0; pos < positionList.size() - 1; pos++) {
                        for (int posOther = pos + 1; posOther < positionList.size(); posOther++) {
                            ArrayList<Integer> cnf = new ArrayList<Integer>();
                            cnf.add(-idMap.get(makeStringId("W", i, positionList.get(pos))));
                            cnf.add(-idMap.get(makeStringId("W", i, positionList.get(posOther))));
                            // System.out.print(makeStringId("W", i, positionList.get(pos)) + " ");
                            // System.out.print(makeStringId("W", i, positionList.get(posOther)) + ", ");
                            cnfClause.add(ArrayListToArray(cnf));
                        }
                    }
                    // System.out.println();
                }

            }

            for (int duplicateValues : duplicateMapRow.keySet()) {
                ArrayList<Integer> positionList = duplicateMapRow.get(duplicateValues);
                if (positionList.size() > 1) {
                    // convolution combination 2 of number of duplicateValues
                    for (int pos = 0; pos < positionList.size() - 1; pos++) {
                        for (int posOther = pos + 1; posOther < positionList.size(); posOther++) {
                            ArrayList<Integer> cnf = new ArrayList<Integer>();
                            cnf.add(-idMap.get(makeStringId("W", positionList.get(pos), i)));
                            cnf.add(-idMap.get(makeStringId("W", positionList.get(posOther), i)));
                            // System.out.print(makeStringId("W", positionList.get(pos), i) + " ");
                            // System.out.print(makeStringId("W", positionList.get(posOther), i) + ", ");
                            cnfClause.add(ArrayListToArray(cnf));
                        }
                    }
                    // System.out.println();
                }
            }
            // System.out.println();
        }
    }

    public void GenerateSecondRuleClause() {
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                ArrayList<Integer> cnfColumn = new ArrayList<Integer>();
                ArrayList<Integer> cnfRow = new ArrayList<Integer>();
                if (i != matrixSize - 1 && j != matrixSize - 1) {
                    cnfRow.add(idMap.get(makeStringId("W", i, j)));
                    cnfRow.add(idMap.get(makeStringId("W", i, j + 1)));
                    cnfColumn.add(idMap.get(makeStringId("W", i, j)));
                    cnfColumn.add(idMap.get(makeStringId("W", i + 1, j)));
                    cnfClause.add(ArrayListToArray(cnfRow));
                    cnfClause.add(ArrayListToArray(cnfColumn));

                }
                if (i != matrixSize - 1 && j == matrixSize - 1) {
                    cnfColumn.add(idMap.get(makeStringId("W", i, j)));
                    cnfColumn.add(idMap.get(makeStringId("W", i + 1, j)));
                    cnfClause.add(ArrayListToArray(cnfColumn));
                }
                if (i == matrixSize - 1 && j != matrixSize - 1) {
                    cnfRow.add(idMap.get(makeStringId("W", i, j)));
                    cnfRow.add(idMap.get(makeStringId("W", i, j + 1)));
                    cnfClause.add(ArrayListToArray(cnfRow));
                }

            }
        }
    }

    private void GenerateThirdRuleClause() {
        for (int i = 0; i < matrixSize - 1; i++) {
            for (int j = 0; j < matrixSize; j++) {
                int currentWID = idMap.get(makeStringId("W", i, j));
                if (j == 0) {
                    ArrayList<Integer> rightBottomBlackCellCNF1 = new ArrayList<>();
                    ArrayList<Integer> rightBottomBlackCellCNF2 = new ArrayList<>();
                    ArrayList<Integer> rightBottomBlackCellCNF3 = new ArrayList<>();
                    ArrayList<Integer> rightBottomBlackCellCNF4 = new ArrayList<>();
                    int rightBottomWID = idMap.get(makeStringId("W", i + 1, j + 1));
                    int currentCID = idMap.get(makeStringId("C", i, j, i + 1, j + 1));
                    int rightBottomCID = idMap.get(makeStringId("C", i + 1, j + 1, i, j));
                    rightBottomBlackCellCNF1.add(-currentCID);
                    rightBottomBlackCellCNF1.add(-rightBottomCID);
                    rightBottomBlackCellCNF1.add(currentWID);

                    rightBottomBlackCellCNF2.add(-currentCID);
                    rightBottomBlackCellCNF2.add(-rightBottomCID);
                    rightBottomBlackCellCNF2.add(rightBottomWID);

                    rightBottomBlackCellCNF3.add(currentCID);
                    rightBottomBlackCellCNF3.add(rightBottomCID);
                    rightBottomBlackCellCNF3.add(currentWID);

                    rightBottomBlackCellCNF4.add(currentCID);
                    rightBottomBlackCellCNF4.add(rightBottomCID);
                    rightBottomBlackCellCNF4.add(rightBottomWID);

                    cnfClause.add(ArrayListToArray(rightBottomBlackCellCNF1));
                    cnfClause.add(ArrayListToArray(rightBottomBlackCellCNF2));
                    cnfClause.add(ArrayListToArray(rightBottomBlackCellCNF3));
                    cnfClause.add(ArrayListToArray(rightBottomBlackCellCNF4));

                } else if (j == matrixSize - 1) {
                    ArrayList<Integer> leftBottomBlackCellCNF1 = new ArrayList<>();
                    ArrayList<Integer> leftBottomBlackCellCNF2 = new ArrayList<>();
                    ArrayList<Integer> leftBottomBlackCellCNF3 = new ArrayList<>();
                    ArrayList<Integer> leftBottomBlackCellCNF4 = new ArrayList<>();
                    int leftBottomWID = idMap.get(makeStringId("W", i + 1, j - 1));
                    int currentCID = idMap.get(makeStringId("C", i, j, i + 1, j - 1));
                    int leftBottomCID = idMap.get(makeStringId("C", i + 1, j - 1, i, j));
                    leftBottomBlackCellCNF1.add(-currentCID);
                    leftBottomBlackCellCNF1.add(-leftBottomCID);
                    leftBottomBlackCellCNF1.add(currentWID);

                    leftBottomBlackCellCNF2.add(-currentCID);
                    leftBottomBlackCellCNF2.add(-leftBottomCID);
                    leftBottomBlackCellCNF2.add(leftBottomWID);

                    leftBottomBlackCellCNF3.add(currentCID);
                    leftBottomBlackCellCNF3.add(leftBottomCID);
                    leftBottomBlackCellCNF3.add(currentWID);

                    leftBottomBlackCellCNF4.add(currentCID);
                    leftBottomBlackCellCNF4.add(leftBottomCID);
                    leftBottomBlackCellCNF4.add(leftBottomWID);

                    cnfClause.add(ArrayListToArray(leftBottomBlackCellCNF1));
                    cnfClause.add(ArrayListToArray(leftBottomBlackCellCNF2));
                    cnfClause.add(ArrayListToArray(leftBottomBlackCellCNF3));
                    cnfClause.add(ArrayListToArray(leftBottomBlackCellCNF4));
                } else {
                    ArrayList<Integer> rightBottomBlackCellCNF1 = new ArrayList<>();
                    ArrayList<Integer> rightBottomBlackCellCNF2 = new ArrayList<>();
                    ArrayList<Integer> rightBottomBlackCellCNF3 = new ArrayList<>();
                    ArrayList<Integer> rightBottomBlackCellCNF4 = new ArrayList<>();
                    int rightBottomWID = idMap.get(makeStringId("W", i + 1, j + 1));
                    int currentRightBottomCID = idMap.get(makeStringId("C", i, j, i + 1, j + 1));
                    int rightBottomCID = idMap.get(makeStringId("C", i + 1, j + 1, i, j));
                    rightBottomBlackCellCNF1.add(-currentRightBottomCID);
                    rightBottomBlackCellCNF1.add(-rightBottomCID);
                    rightBottomBlackCellCNF1.add(currentWID);

                    rightBottomBlackCellCNF2.add(-currentRightBottomCID);
                    rightBottomBlackCellCNF2.add(-rightBottomCID);
                    rightBottomBlackCellCNF2.add(rightBottomWID);

                    rightBottomBlackCellCNF3.add(currentRightBottomCID);
                    rightBottomBlackCellCNF3.add(rightBottomCID);
                    rightBottomBlackCellCNF3.add(currentWID);

                    rightBottomBlackCellCNF4.add(currentRightBottomCID);
                    rightBottomBlackCellCNF4.add(rightBottomCID);
                    rightBottomBlackCellCNF4.add(rightBottomWID);

                    ArrayList<Integer> leftBottomBlackCellCNF1 = new ArrayList<>();
                    ArrayList<Integer> leftBottomBlackCellCNF2 = new ArrayList<>();
                    ArrayList<Integer> leftBottomBlackCellCNF3 = new ArrayList<>();
                    ArrayList<Integer> leftBottomBlackCellCNF4 = new ArrayList<>();
                    int leftBottomWID = idMap.get(makeStringId("W", i + 1, j - 1));
                    int currentLeftBottomCID = idMap.get(makeStringId("C", i, j, i + 1, j - 1));
                    int leftBottomCID = idMap.get(makeStringId("C", i + 1, j - 1, i, j));
                    leftBottomBlackCellCNF1.add(-currentLeftBottomCID);
                    leftBottomBlackCellCNF1.add(-leftBottomCID);
                    leftBottomBlackCellCNF1.add(currentWID);

                    leftBottomBlackCellCNF2.add(-currentLeftBottomCID);
                    leftBottomBlackCellCNF2.add(-leftBottomCID);
                    leftBottomBlackCellCNF2.add(leftBottomWID);

                    leftBottomBlackCellCNF3.add(currentLeftBottomCID);
                    leftBottomBlackCellCNF3.add(leftBottomCID);
                    leftBottomBlackCellCNF3.add(currentWID);

                    leftBottomBlackCellCNF4.add(currentLeftBottomCID);
                    leftBottomBlackCellCNF4.add(leftBottomCID);
                    leftBottomBlackCellCNF4.add(leftBottomWID);

                    cnfClause.add(ArrayListToArray(rightBottomBlackCellCNF1));
                    cnfClause.add(ArrayListToArray(rightBottomBlackCellCNF2));
                    cnfClause.add(ArrayListToArray(rightBottomBlackCellCNF3));
                    cnfClause.add(ArrayListToArray(rightBottomBlackCellCNF4));
                    cnfClause.add(ArrayListToArray(leftBottomBlackCellCNF1));
                    cnfClause.add(ArrayListToArray(leftBottomBlackCellCNF2));
                    cnfClause.add(ArrayListToArray(leftBottomBlackCellCNF3));
                    cnfClause.add(ArrayListToArray(leftBottomBlackCellCNF4));
                }

            }
        }
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                for (int k = 0; k < matrixSize; k++) {
                    for (int l = 0; l < matrixSize; l++) {
                        int ijklID = idMap.get(makeStringId("C", i, j, k, l));
                        for (int m = 0; m < matrixSize; m++) {
                            for (int n = 0; n < matrixSize; n++) {
                                int klmnID = idMap.get(makeStringId("C", k, l, m, n));
                                int ijmnID = idMap.get(makeStringId("C", i, j, m, n));
                                ArrayList<Integer> cnf = new ArrayList<>();
                                cnf.add(-ijklID);
                                cnf.add(-klmnID);
                                cnf.add(ijmnID);
                                cnfClause.add(ArrayListToArray(cnf));
                            }
                        }
                    }
                }
            }
        }
    }

    private void SAT4JSolver() throws ContradictionException {
        ISolver solver = SolverFactory.newDefault();
        try {

            for (int[] element : cnfClause) {
                solver.addClause(new VecInt(element));
            }

            long startTime = System.currentTimeMillis();
            boolean isSAT = solver.isSatisfiable();
            long endTime = System.currentTimeMillis();

            this.time = endTime - startTime;

            if (isSAT) {
                int[] model = solver.model();
                for (int i = 0; i < matrixSize * matrixSize; i++) {
                    String a = "";
                    for (String stringID : idMap.keySet()) {
                        if (idMap.get(stringID) == i + 1) {
                            a = stringID;
                            //System.out.println(a);
                        }
                    }
                    //if (model[i] > 0) {
                        System.out.println(a + ": " + model[i]);
                    //}
                    
                }

            } else {
                System.out.println("UNSAT");
            }
        } catch (TimeoutException e) {
            System.out.println("Timeout");
        }
    }

    private String makeStringId(String type, int i, int j, int... args) {
        if (type == "C") {
            int k = args[0];
            int l = args[1];
            return type + "." + Integer.toString(i + 1) + "." + Integer.toString(j + 1) + "." + Integer.toString(k + 1)
                    + "." + Integer.toString(l + 1);
        }
        return type + "." + Integer.toString(i + 1) + "." + Integer.toString(j + 1) + "."
                + Integer.toString(input[i][j]);
    }

    private int CreateOrGetNumberID(String stringID) {
        if (!idMap.containsKey(stringID)) {
            numberOfVariable++;
            idMap.put(stringID, numberOfVariable);
            return numberOfVariable;
        }
        return idMap.get(stringID);
    }

    private int[] ArrayListToArray(ArrayList<Integer> arrayList) {
        int[] array = new int[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++) {
            array[i] = arrayList.get(i);
        }
        return array;
    }

    public void PrintClause() {
        for (int[] element : cnfClause) {
            PrintArray(element);
        }
    }

    public void PrintArray(int[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + " ");
        }
        System.out.println();
    }
}
