package fir.evt;

import fir.vo.Point;

/**
 * Created by kongzheng on 16/8/31.
 */
public class TurnToHumanEvent extends AbstractSituationEvent {

    public TurnToHumanEvent(byte[][] matrix, Point point, int step) {
        super(matrix, point, step);
    }
}
