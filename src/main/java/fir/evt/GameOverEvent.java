package fir.evt;

import fir.ChessBoard;

/**
 * Created by kongzheng on 16/8/30.
 */
public class GameOverEvent implements Event {

    public Result result;

    public GameOverEvent(Result result) {
        this.result = result;
    }

    public enum Result {
        draw, whiteWin, blackWin, nobodyWin;

        public static Result fromColor(int color) {
            return color == ChessBoard.BLACK ? blackWin : (color == ChessBoard.WHITE ? whiteWin : nobodyWin);
        }
    }
}
