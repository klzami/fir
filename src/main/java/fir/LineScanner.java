package fir;

import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;
import fir.vo.Line;
import fir.vo.Point;

import java.util.Arrays;
import java.util.List;

/**
 * 棋线扫描器
 * 扫描范围: # # # # $ # # # #
 * 其中#代表非当前点,$代表当前点
 * 横、竖、左对角线、右对角线四个方向扫描
 * 注意: 该对象不可重用!
 */
public class LineScanner {

    private final byte[][] matrix;
    private int phase = NULL;//扫描的阶段: 横 -> 竖 -> 左对角线 -> 右对角线

    private static final int NULL = 0;//空的
    private static final int HORIZONTAL = 1;//水平(横向)
    private static final int VERTICAL = 2;//垂直(竖向)
    private static final int LEFT_TOP_DIAGONAL = 3;//左上对角线(从左上指向右下)
    private static final int RIGHT_TOP_DIAGONAL = 4;//右上对角线(从右上指向左下)

    public LineScanner(byte[][] matrix) {
        this.matrix = matrix;
    }

    public void reset() {
        phase = NULL;
    }

    public boolean hasNextLine() {
        return phase < RIGHT_TOP_DIAGONAL;
    }

    public Line nextLine(Point p, int maxOffset) {
        int i = p.i;
        int j = p.j;
        int baseIndex;
        byte[] array;
        phase++;
        int start;
        int end;
        switch (phase) {//0, 1, 2, 3, 4, 5, [6], 7, 8, 9, 10, 11
            case HORIZONTAL://0, 1, [2], 3, 4, 5, 6, 7
                start = Math.max(j - maxOffset, 0);
                end = Math.min(j + maxOffset + 1, 15);
                array = Arrays.copyOfRange(matrix[i], start, end);//拷贝范围: [start, end)
                baseIndex = (j >= maxOffset ? maxOffset : j);
                break;
            case VERTICAL:
                byte[] vColors = new byte[15];
                for (int k = 0; k < 15; k++) {
                    vColors[k] = matrix[k][j];
                }
                start = Math.max(i - maxOffset, 0);
                end = Math.min(i + maxOffset + 1, 15);
                array = Arrays.copyOfRange(vColors, start, end);//拷贝范围: [start, end)
                baseIndex = (i >= maxOffset ? maxOffset : i);
                break;
            case LEFT_TOP_DIAGONAL:
                int i2 = i;
                int j2 = j;
                List<Byte> ltList = Lists.newLinkedList();
                ltList.add(matrix[p.i][p.j]);
                int cnt = 0;
                for (int k = 0; k < 15; k++) {
                    if (++i2 >= 15 || ++j2 >= 15 || cnt >= maxOffset) {
                        break;
                    } else {
                        ltList.add(matrix[i2][j2]);
                        cnt++;
                    }
                }
                i2 = i;
                j2 = j;
                cnt = 0;
                for (int k = 0; k < 15; k++) {
                    if (--i2 < 0 || --j2 < 0 || cnt >= maxOffset) {
                        break;
                    } else {
                        ltList.add(0, matrix[i2][j2]);
                        cnt++;
                    }
                }
                array = Bytes.toArray(ltList);
                baseIndex = cnt;
                break;
            case RIGHT_TOP_DIAGONAL:
                int i3 = i;
                int j3 = j;
                List<Byte> rtList = Lists.newLinkedList();
                rtList.add(matrix[p.i][p.j]);
                int cnt2 = 0;
                for (int k = 0; k < 15; k++) {
                    if (--i3 < 0 || ++j3 >= 15 || cnt2 >= maxOffset) {
                        break;
                    } else {
                        rtList.add(matrix[i3][j3]);
                        cnt2++;
                    }
                }
                i3 = i;
                j3 = j;
                cnt2 = 0;
                for (int k = 0; k < 15; k++) {
                    if (++i3 >= 15 || --j3 < 0 || cnt2 >= maxOffset) {
                        break;
                    } else {
                        rtList.add(0, matrix[i3][j3]);
                        cnt2++;
                    }
                }
                array = Bytes.toArray(rtList);
                baseIndex = cnt2;
                break;
            default:
                throw new UnsupportedOperationException("内部状态错误");
        }
        return new Line(array, baseIndex);
    }
}