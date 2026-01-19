package org.example.sorting_of_liquids;

import java.util.*;

public class State {
    private final List<Tube> tubes;
    private final List<Move> moves;

    public State(List<Tube> tubes) {
        this.tubes = new ArrayList<>();
        for (Tube t : tubes) this.tubes.add(t.copy());
        this.moves = new ArrayList<>();
    }

    public State(State other) {
        this.tubes = new ArrayList<>();
        for (Tube t : other.tubes) this.tubes.add(t.copy());
        this.moves = new ArrayList<>(other.moves);
    }

    public List<Tube> getTubes() {
        return tubes;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public void addMove(Move move) {
        moves.add(move);
    }

    public boolean isSolved() {
        for (Tube t : tubes)
            if (!t.isSolved()) return false;
        return true;
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        for (Tube t : tubes) {
            for (char c : t.getLiquids()) sb.append(c);
            sb.append('|');
        }
        return sb.toString();
    }
}
