package org.example.sorting_of_liquids;

import java.util.*;

public class Tube {
    private final int capacity;
    private final List<Character> liquids;

    public Tube(int capacity) {
        this.capacity = capacity;
        this.liquids = new ArrayList<>();
    }

    public Tube(int capacity, List<Character> liquids) {
        this.capacity = capacity;
        this.liquids = new ArrayList<>(liquids);
    }

    public Tube copy() {
        return new Tube(capacity, liquids);
    }

    public boolean isEmpty() {
        return liquids.isEmpty();
    }

    public boolean isFull() {
        return liquids.size() == capacity;
    }

    public int freeSpace() {
        return capacity - liquids.size();
    }

    public char topColor() {
        return liquids.get(liquids.size() - 1);
    }

    public int countTopSameColor() {
        if (isEmpty()) return 0;
        char c = topColor();
        int count = 0;
        for (int i = liquids.size() - 1; i >= 0; i--) {
            if (liquids.get(i) == c) count++;
            else break;
        }
        return count;
    }

    public boolean canPourTo(Tube other) {
        if (isEmpty() || other.isFull()){
            return false;
        }
        return other.isEmpty() || other.topColor() == topColor();
    }

    public List<Character> removeTop(int n) {
        List<Character> res = new ArrayList<>(liquids.subList(liquids.size() - n, liquids.size()));
        liquids.subList(liquids.size() - n, liquids.size()).clear();
        return res;
    }

    public void add(List<Character> items) {
        liquids.addAll(items);
    }

    public boolean isSolved() {
        if (isEmpty()){
            return true;
        }
        if (liquids.size() != capacity){
            return false;
        }
        char c = liquids.get(0);
        for (char x : liquids) if (x != c){
            return false;
        }
        return true;
    }

    public List<Character> getLiquids() {
        return liquids;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tube)) return false;
        return liquids.equals(((Tube) o).liquids);
    }

    @Override
    public int hashCode() {
        return liquids.hashCode();
    }
}

