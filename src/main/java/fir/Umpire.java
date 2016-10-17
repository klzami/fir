package fir;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import fir.evt.AfterGoEvent;
import fir.evt.GameOverEvent;
import fir.evt.TurnToHumanEvent;
import fir.evt.TurnToRobotEvent;
import fir.vo.Line;

/**
 * 裁判
 * Created by kongzheng on 16/8/30.
 */
public class Umpire {

    private EventBus bus;

    public Umpire setBus(EventBus bus) {
        this.bus = bus;
        return this;
    }

    @Subscribe
    public void onEvent(AfterGoEvent e) {
        LineScanner scanner = new LineScanner(e.matrix);
        while (scanner.hasNextLine()) {
            Line line = scanner.nextLine(e.point, 4);
            int sum = 0;
            for (int i = line.origin + 1; i < line.colors.length; i++) {
                if (line.colors[i] == e.matrix[e.point.i][e.point.j]) {
                    sum++;
                } else {
                    break;
                }
            }
            for (int i = line.origin; i >= 0; i--) {
                if (line.colors[i] == e.matrix[e.point.i][e.point.j]) {
                    sum++;
                } else {
                    break;
                }
            }
            if (sum >= 5) {
                bus.post(new GameOverEvent(GameOverEvent.Result.fromColor(e.matrix[e.point.i][e.point.j])));
                return;
            }
        }
        if (e.step >= 15 * 15) {
            bus.post(new GameOverEvent(GameOverEvent.Result.draw));
            return;
        }
        if (e.matrix[e.point.i][e.point.j] == ChessBoard.BLACK) {
            bus.post(new TurnToRobotEvent(e.matrix, e.point, e.step));
        } else {
            bus.post(new TurnToHumanEvent(e.matrix, e.point, e.step));
        }
    }

}
