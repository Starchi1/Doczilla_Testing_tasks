package org.example.sorting_of_liquids;


import java.util.*;

public class Main {
    public static void main(String[] args) {

        int V = 4;
        char[][] input = {
                {'A', 'B', 'C', 'D'},
                {'A', 'B', 'C', 'D'},
                {'A', 'B', 'C', 'D'},
                {'A', 'B', 'C', 'D'},
                {},
                {}
        };

        List<Tube> tubes = new ArrayList<>();
        for (char[] arr : input) {
            List<Character> l = new ArrayList<>();
            for (char c : arr) l.add(c);
            tubes.add(new Tube(V, l));
        }

        State initial = new State(tubes);
        Solver solver = new Solver(V);

        State solution = solver.solveAStar(initial);

        if (solution != null) {
            for (Move m : solution.getMoves()) {
                System.out.print(m + " ");
            }
            System.out.println("\nВсего ходов: " + solution.getMoves().size());
        } else {
            System.out.println("Решение не найдено.");
        }
    }
}
