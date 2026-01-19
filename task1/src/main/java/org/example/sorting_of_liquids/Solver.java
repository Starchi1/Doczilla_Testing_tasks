package org.example.sorting_of_liquids;
import java.util.*;

public class Solver {
    private final int tubeCapacity;

    public Solver(int tubeCapacity) {
        this.tubeCapacity = tubeCapacity;
    }

    public State solveAStar(State start) {
        class Node {
            State state;
            int g, h, f;
            Node(State s) {
                state = s;
                g = s.getMoves().size();
                h = heuristic(s);
                f = g + h;
            }
        }

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Map<String, Integer> bestG = new HashMap<>();

        Node startNode = new Node(start);
        open.add(startNode);
        bestG.put(start.serialize(), 0);

        while (!open.isEmpty()) {
            Node current = open.poll();
            State cur = current.state;

            if (cur.isSolved()) return cur;

            List<Tube> tubes = cur.getTubes();
            for (int i = 0; i < tubes.size(); i++) {
                Tube src = tubes.get(i);
                if (src.isEmpty() || src.isSolved()) continue;

                char color = src.topColor();
                int count = src.countTopSameColor();

                for (int j = 0; j < tubes.size(); j++) {
                    if (i == j) continue;
                    Tube dst = tubes.get(j);
                    if (dst.isFull()) continue;
                    if (!dst.isEmpty() && dst.topColor() != color) continue;

                    int moveCount = Math.min(count, dst.freeSpace());
                    if (moveCount == 0) continue;

                    State next = new State(cur);
                    List<Character> moved = next.getTubes().get(i).removeTop(moveCount);
                    next.getTubes().get(j).add(moved);
                    next.addMove(new Move(i, j));

                    String key = next.serialize();
                    int newG = next.getMoves().size();
                    if (!bestG.containsKey(key) || newG < bestG.get(key)) {
                        bestG.put(key, newG);
                        open.add(new Node(next));
                    }
                }
            }
        }
        return null;
    }

    private int heuristic(State s) {
        int h = 0;
        for (Tube t : s.getTubes()) {
            List<Character> liq = t.getLiquids();
            if (liq.isEmpty()) continue;
            for (int i = 1; i < liq.size(); i++) {
                if (liq.get(i) != liq.get(i-1)) h++;
            }
            if (liq.size() < tubeCapacity && isUniform(liq)) h++;
        }
        return h;
    }

    private boolean isUniform(List<Character> tube) {
        char c = tube.get(0);
        for (char x : tube) if (x != c) return false;
        return true;
    }
}
