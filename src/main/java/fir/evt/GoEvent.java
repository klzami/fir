package fir.evt;

import fir.vo.Point;

/**
 * Created by kongzheng on 16/8/31.
 */
public class GoEvent implements Event {

    public Point point;
    public byte color;

    public GoEvent(Point point, byte color) {
        this.point = point;
        this.color = color;
    }
}
