package fir.vo;

public class Line {
    public byte[] colors;
    public int origin;//在取出的直线中,当前基准点的下标索引值

    public Line(byte[] colors, int origin) {
        this.colors = colors;
        this.origin = origin;
    }
}