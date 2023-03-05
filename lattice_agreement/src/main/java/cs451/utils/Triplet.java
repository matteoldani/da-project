package cs451.utils;

import java.util.Objects;

public class Triplet <T, U, V>{

    private T first;
    private U second;
    private V third;

    public Triplet(T first, U second, V third){
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public T getFirst() {
        return first;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public U getSecond() {
        return second;
    }

    public void setSecond(U second) {
        this.second = second;
    }

    public V getThird() {
        return third;
    }

    public void setThird(V third) {
        this.third = third;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) o;
        return first.equals(triplet.first) && second.equals(triplet.second) && third.equals(triplet.third);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, third);
    }
}
