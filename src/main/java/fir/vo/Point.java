package fir.vo;

public class Point {
    public int i;
    public int j;

    public Point(int i, int j) {
        this.i = i;
        this.j = j;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        if (i != point.i) return false;
        return j == point.j;

    }

    @Override
    public int hashCode() {
        int result = i;
        result = 31 * result + j;
        return result;
    }

    @Override
    public String toString() {
        char x = (char) (65 + i);
        int y = 15 - j;
        return "(" + x + ", " + y + ")";
    }

    public double distanceOf(int i, int j) {
        return Math.max(this.i - i, this.j - j);
    }

    public double distanceOf(Point that) {
        return distanceOf(that.i, that.j);
    }
}