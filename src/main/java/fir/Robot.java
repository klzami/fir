package fir;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import fir.evt.GoEvent;
import fir.evt.TurnToRobotEvent;
import fir.evt.UndoEvent;
import fir.vo.Line;
import fir.vo.Point;
import fir.vo.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static fir.ChessBoard.BLACK;
import static fir.ChessBoard.EMPTY;
import static fir.ChessBoard.WHITE;

/**
 * Created by kongzheng on 16/8/30.
 */
public class Robot {//TODO bugfix 棋型判断仍有bug

    private static final Logger log = LoggerFactory.getLogger(Robot.class);

    private ShapeAnalyzer shapeAnalyzer;
    private EventBus bus;

    public Robot setBus(EventBus bus) {
        this.bus = bus;
        return this;
    }

    public Robot() {
        this.shapeAnalyzer = new ShapeAnalyzer();
    }

    private static final ImmutableMap<Shape, Integer> SCORE_MAP = new ImmutableMap.Builder<Shape, Integer>()
            .put(Shape.rubbish, 1)
            .put(Shape.blockedOne, 10)
            .put(Shape.one, 25)
            .put(Shape.blockedTwo, 20)
            .put(Shape.two, 60)
            .put(Shape.three, 150)
            .put(Shape.blockedThree, 50)
            .put(Shape.blockedFour, 120)
            .build();

    private static final Map<Point, List<Line>> LINES_CACHE = new HashMap<Point, List<Line>>(225);
    private static final Map<Point, Shape> BLACK_SHAPE_CACHE = new HashMap<Point, Shape>(225);
    private static final Map<Point, Shape> WHITE_SHAPE_CACHE = new HashMap<Point, Shape>(225);


    /**
     * 电脑下的是后手棋,因此,可以利用这一点,简化AI
     *
     * @param e
     */
    @Subscribe
    public void onEvent(TurnToRobotEvent e) {
        Point target = findOptimalPoint(e.matrix, e.point);
        bus.post(new GoEvent(target, ChessBoard.WHITE));
    }

    @Subscribe
    public void onEvent(UndoEvent e) {
        LINES_CACHE.clear();
    }

    protected Point findOptimalPoint(byte[][] matrix, Point currentPoint) {
        LineScanner scanner = new LineScanner(matrix);

        //几个特殊棋型能不能成
        final LinkedList<Point> blackFives = Lists.newLinkedList();
        final LinkedList<Point> whiteFours = Lists.newLinkedList();
        final LinkedList<Point> blackFours = Lists.newLinkedList();
        final LinkedList<Point> whiteThrees = Lists.newLinkedList();
        final LinkedList<Point> blackThrees = Lists.newLinkedList();
        final List<LinkedList<Point>> infosList = Lists.newArrayList(blackFives, whiteFours, blackFours, whiteThrees, blackThrees);

        boolean collectedResult = false;
        Point maxScorePoint = null;
        int maxScore = Integer.MIN_VALUE;
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                Point point = new Point(i, j);
                //TODO 缓存计算结果
                if (matrix[i][j] != ChessBoard.EMPTY) {
                    continue;
                }

                List<Line> lines = scanLines(scanner, point);

                //2格以内,必须至少跟一颗棋子相邻
                if (!hasNeighbor(lines)) {
                    continue;
                }

                int whiteThreeCount = 0;
                int blackThreeCount = 0;
                int dc = 0;
                int ac = 0;
                for (Line line : lines) {
                    //看看我是什么棋型
                    Shape whiteShape = analyzeShape(line, WHITE);
//                    Shape whiteShape;
//                    if (canUseCache) {
//                        if (WHITE_SHAPE_CACHE.containsKey(point)) {
//                            whiteShape = WHITE_SHAPE_CACHE.get(point);
//                        } else {
//                            whiteShape = analyzeShape(line, WHITE);
//                            WHITE_SHAPE_CACHE.put(point, whiteShape);
//                        }
//                    } else {
//                        whiteShape = analyzeShape(line, WHITE);
//                    }

                    //我能不能成5?
                    if (whiteShape == Shape.five) {
                        debug("我方可成5: {}", point);
                        return point;
                    }

                    //看看敌人是什么棋型
                    Shape blackShape = analyzeShape(line, BLACK);
//                    Shape blackShape;
//                    if (canUseCache) {
//                        if (BLACK_SHAPE_CACHE.containsKey(point)) {
//                            blackShape = BLACK_SHAPE_CACHE.get(point);
//                        } else {
//                            blackShape = analyzeShape(line, BLACK);
//                            BLACK_SHAPE_CACHE.put(point, whiteShape);
//                        }
//                    } else {
//                        blackShape = analyzeShape(line, BLACK);
//                    }

                    //其他棋型被扫到,先存起来
                    //敌人能不能成5?
                    if (blackShape == Shape.five) {
                        debug("敌方可成5: {}", point);
                        collectedResult = true;
                        blackFives.add(point);
                    }
                    //我能不能成活4?
                    if (whiteShape == Shape.four) {
                        debug("我方可成活4: {}", point);
                        collectedResult = true;
                        whiteFours.add(point);
                    }
                    //敌人能不能成活4?
                    if (blackShape == Shape.four) {
                        debug("敌方可成活4: {}", point);
                        collectedResult = true;
                        blackFours.add(point);
                    }
                    //我能不能成双活3?
                    //死四也当成是活三
                    if (whiteShape == Shape.three || whiteShape == Shape.blockedFour) {
                        if (++whiteThreeCount >= 2) {
                            debug("我方可成冲3: {}", point);
                            collectedResult = true;
                            whiteThrees.add(point);
                        }
                    }
                    //敌人能不能成双活3?
                    //死四也当成是活三
                    if (blackShape == Shape.three || blackShape == Shape.blockedFour) {
                        if (++blackThreeCount >= 2) {
                            debug("敌方可成冲3: {}", point);
                            collectedResult = true;
                            blackThrees.add(point);
                        }
                    }

                    //计算这条线的得分
                    if (!collectedResult) {
                        //假设下一枚黑棋,算一遍防守得分
                        dc += SCORE_MAP.get(blackShape);

                        //假设下一枚白棋,算一遍进攻得分
                        ac += SCORE_MAP.get(whiteShape);
                    }

                    //还原假设
                    line.colors[line.origin] = EMPTY;
                }

                //找出得分最高的点
                if (!collectedResult) {
                    //两者结合,得出总分
                    double d = point.distanceOf(7, 7);//距离中心点越近越好
                    int score = (int) (ac + dc - d);
                    if (score > maxScore) {
                        maxScore = score;
                        maxScorePoint = point;
                    }
                }
            }
        }

        for (LinkedList<Point> infos : infosList) {
            if (!infos.isEmpty()) {
                return infos.getFirst();
            }
        }

        return maxScorePoint;
    }

    private Shape analyzeShape(Line line, byte myColor) {
        line.colors[line.origin] = myColor;
        Shape shape = shapeAnalyzer.analyze(line.colors, line.origin, myColor);
        return shape;
    }

    private List<Line> scanLinesWithCache(LineScanner scanner, Point currentPoint, Point point) {
        final boolean canUseCache = (currentPoint.distanceOf(point) > 4);
        List<Line> lines;
        if (canUseCache) {
            if (LINES_CACHE.containsKey(point)) {
                lines = LINES_CACHE.get(point);
            } else {
                lines = scanLines(scanner, point);
                LINES_CACHE.put(point, lines);
            }
        } else {
            lines = scanLines(scanner, point);
        }
        return lines;
    }

    private List<Line> scanLines(LineScanner scanner, Point point) {
        List<Line> lines = Lists.newLinkedList();
        scanner.reset();
        while (scanner.hasNextLine()) {
            Line line = scanner.nextLine(point, 4);
            lines.add(line);
        }
        return lines;
    }

    private static void debug(String msg, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug(msg, args);
        }
    }

    private boolean hasNeighbor(List<Line> lines) {
        for (Line line : lines) {
            byte[] colors = line.colors;
            int baseIndex = line.origin;
            int start = baseIndex - 2;
            int end = baseIndex + 2;

            //这种情况说明在边角上,不要求有相邻的点
            if (start < 0 || end >= colors.length) {
                return true;
            }

            for (int i = start; i <= baseIndex + 2; i++) {
                if (i == baseIndex) {
                    continue;
                }
                if (colors[i] != EMPTY) {
                    return true;
                }
            }
        }
        return false;
    }
}
