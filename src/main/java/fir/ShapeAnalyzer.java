package fir;

import fir.vo.Shape;

import static fir.ChessBoard.BLACK;
import static fir.ChessBoard.EMPTY;
import static fir.ChessBoard.WHITE;

/**
 * 棋型分析器
 * Created by kongzheng on 16/9/3.
 */
public class ShapeAnalyzer {

    private static final Shape[] SHAPES = Shape.values();

    public static Shape analyze(byte[] colors, int origin, byte myColor) {
        Shape shape = null;
        int enemyColor = (myColor == BLACK ? WHITE : BLACK);
        int firstMyColorIndex = reverseIndexOf(colors, myColor, enemyColor, origin, 0);
        int lastMyColorIndex = indexOf(colors, myColor, enemyColor, origin, colors.length - 1);
        int chessmanCount = (firstMyColorIndex >= 0) ? (firstMyColorIndex != lastMyColorIndex ? 2 : 1) : 0;
        int preEmptyIndex = -1;
        int emptyCount = 0;//我方棋子中间的空格数,是评估棋型质量的重要标准
        int consequentEmptyCount = 0;//连续空格数

        if ((firstMyColorIndex < 0 && lastMyColorIndex < 0) || (firstMyColorIndex == colors.length - 1)) {//角落里
            return Shape.rubbish;
        }

        for (int i = firstMyColorIndex + 1; i < lastMyColorIndex; i++) {
            if (colors[i] == EMPTY) {
                emptyCount++;
                if (preEmptyIndex >= 0 && preEmptyIndex + 1 == i) {//连续的空格
                    consequentEmptyCount++;
                }
                preEmptyIndex = i;
            } else if (colors[i] == myColor) {
                chessmanCount++;
            }
        }

        int leftEmptyCount = 0;
        if (firstMyColorIndex > 0) {
            for (int i = firstMyColorIndex - 1; i >= 0; i--) {
                if (colors[i] != EMPTY) {
                    break;
                }
                leftEmptyCount++;
            }
        }

        int rightEmptyCount = 0;
        if (lastMyColorIndex < colors.length - 1) {
            for (int i = lastMyColorIndex + 1; i < colors.length; i++) {
                if (colors[i] != EMPTY) {
                    break;
                }
                rightEmptyCount++;
            }
        }
        if (chessmanCount >= 5) {
            shape = Shape.five;
        } else {
            boolean leftBlocked = (chessmanCount + leftEmptyCount < 5);
            boolean rightBlocked = (chessmanCount + rightEmptyCount < 5);
            switch (chessmanCount) {
                case 0:
                    shape = Shape.rubbish;
                    break;
                case 1:
                    if (leftBlocked && rightBlocked) {
                        shape = Shape.rubbish;
                    } else if (leftBlocked || rightBlocked) {
                        shape = Shape.blockedOne;
                    } else {
                        shape = Shape.one;
                    }
                    break;
                case 2:
                    if (leftBlocked && rightBlocked) {
                        shape = Shape.rubbish;
                    } else if (leftBlocked || rightBlocked) {
                        shape = Shape.blockedTwo;
                    } else {
                        shape = Shape.two;
                    }
                    break;
                case 3:
                    if (leftBlocked && rightBlocked) {
                        shape = Shape.rubbish;
                    } else if (leftBlocked || rightBlocked) {
                        shape = Shape.blockedThree;
                    } else {
                        shape = Shape.three;
                    }
                    break;
                case 4:
                    if (leftBlocked && rightBlocked) {
                        shape = Shape.rubbish;
                    } else if (leftBlocked || rightBlocked) {
                        shape = Shape.blockedFour;
                    } else {
                        shape = Shape.four;
                    }
                    break;

            }
        }
        shape = reduceLevelByEmpty(shape, emptyCount + consequentEmptyCount * 2);
        return shape;
    }

    /**
     * 根据空格数据对棋型进行降级
     *
     * @param shape
     * @param emptyCount
     * @return
     */
    private static Shape reduceLevelByEmpty(Shape shape, int emptyCount) {
        int i = Math.max(shape.ordinal() - emptyCount, 0);
        return SHAPES[i];
    }

    /**
     * 正向查找
     *
     * @param array
     * @param myColor
     * @param enemyColor
     * @param start      包含
     * @param end        包含   @return
     */
    private static int indexOf(byte[] array, byte myColor, int enemyColor, int start, int end) {
        if (start < 0 || start >= array.length || end < 0 || end >= array.length || start > end) {
            return -1;
        }
        int ret = start;
        for (int i = start; i <= end; i++) {
            if (array[i] == enemyColor) {
                break;
            } else if (array[i] == myColor) {
                ret = i;
            }
        }
        return ret;
    }

    /**
     * 反向查找
     *
     * @param array
     * @param myColor
     * @param enemyColor
     * @param start      包含
     * @param end        包含   @return
     */
    private static int reverseIndexOf(byte[] array, byte myColor, int enemyColor, int start, int end) {
        if (start < 0 || start >= array.length || end < 0 || end >= array.length || start < end) {
            return -1;
        }
        int ret = start;
        for (int i = start; i >= end; i--) {
            if (array[i] == enemyColor) {
                break;
            } else if (array[i] == myColor) {
                ret = i;
            }
        }
        return ret;
    }
}
