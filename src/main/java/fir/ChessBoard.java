package fir;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Resources;
import fir.evt.AfterGoEvent;
import fir.evt.GameOverEvent;
import fir.evt.GoEvent;
import fir.evt.PlaySoundEvent;
import fir.evt.ResetSituationEvent;
import fir.evt.TurnToHumanEvent;
import fir.evt.UndoEvent;
import fir.vo.Point;

import javax.naming.OperationNotSupportedException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 棋盘
 * 黑棋先走
 * 人类棋手执黑
 * Created by kongzheng on 16/8/26.
 */
public class ChessBoard extends Canvas {

    public static final Image BACKGROUND_IMG;
    public static final Image BLACK_CHESSMAN_IMG;
    public static final Image WHITE_CHESSMAN_IMG;

    public static final byte EMPTY = 0;
    public static final byte BLACK = 1;
    public static final byte WHITE = 2;
    public static final byte MARGIN_BORDER = 50;
    public static final int MARGIN_RECT = 30;
    public static final int MARGIN = MARGIN_BORDER + MARGIN_RECT;
    public static final byte LINE_SPACING = 40;
    public static final byte CHESS_SIZE = 36;
    public static final int PHASE_GAME_OVER = 1;

    private int phase;
    private EventBus bus;
    private byte[][] matrix = new byte[15][15];
    private int step;
    private List<Point> history = Lists.newArrayListWithCapacity(15 * 15);
    private final AtomicBoolean unlock = new AtomicBoolean(true);

    static {
        BACKGROUND_IMG = new ImageIcon(Resources.getResource("wood.jpg")).getImage();
        BLACK_CHESSMAN_IMG = new ImageIcon(Resources.getResource("b.png")).getImage();
        WHITE_CHESSMAN_IMG = new ImageIcon(Resources.getResource("w.png")).getImage();
    }

    public ChessBoard setBus(EventBus bus) {
        this.bus = bus;
        return this;
    }

    public ChessBoard() {
        super();
        setFocusable(false);
        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                onMouseClicked(e.getX(), e.getY());
            }
        });
    }

    private void onMouseClicked(int x, int y) {
        if (!unlock.get()) {
            System.out.println("还没有轮到你下");
            return;
        }

        // 找出最近的点
        int i, j;
        try {
            i = calculatedClickHitCoordinate(x);
            j = calculatedClickHitCoordinate(y);
        } catch (OperationNotSupportedException e) {
            System.out.println(e.getMessage());
            return;
        }

        if (matrix[i][j] != EMPTY) {
            System.out.println("这里已经有棋子了");
            return;
        }

        if (!unlock.compareAndSet(true, false)) {
            System.out.println("还没有轮到你下");
            return;
        }

        bus.post(new GoEvent(new Point(i, j), BLACK));
    }

    private int calculatedClickHitCoordinate(int n) throws OperationNotSupportedException {
        int r;
        int distance = (n - MARGIN) % LINE_SPACING;
        if (distance > LINE_SPACING / 2) {
            r = (n - MARGIN) / LINE_SPACING + 1;
        } else if (distance == LINE_SPACING / 2) {
            throw new OperationNotSupportedException("点击位置无效,因为此位置在2个点正中间");
        } else {
            r = (n - MARGIN) / LINE_SPACING;
        }
        return r;
    }

    @Subscribe
    public void onEvent(TurnToHumanEvent e) {
        unlock.set(true);
    }

    @Subscribe
    public void onEvent(GoEvent e) {
        //设置状态
        matrix[e.point.i][e.point.j] = e.color;
        step += 1;
        history.add(e.point);

        //重绘
        repaint();

        //呼叫裁判
        bus.post(new AfterGoEvent(matrix, e.point, step));
    }

    @Subscribe
    public void onEvent(AfterGoEvent e) {
        bus.post(new PlaySoundEvent());
    }

    @Subscribe
    public void onEvent(GameOverEvent e) {
        phase = PHASE_GAME_OVER;
        repaint();

        String msgPrefix = null;
        switch (e.result) {
            case draw:
                msgPrefix = "双方打成了平手";
                break;
            case blackWin:
                msgPrefix = "恭喜您获得胜利";
                break;
            case whiteWin:
                msgPrefix = "您输了,电脑获胜";
                break;
            default:
                break;
        }
        int flag = JOptionPane.showConfirmDialog(this, msgPrefix + "。是否重新开始?", "游戏结束", JOptionPane.OK_CANCEL_OPTION);
        if (flag == JOptionPane.OK_OPTION) {
            bus.post(new ResetSituationEvent());
        }
    }

    @Subscribe
    public void onEvent(ResetSituationEvent e) {
        unlock.set(false);
        step = 0;
        phase = 0;
        for (Point p : history) {
            matrix[p.i][p.j] = EMPTY;
        }
        history.clear();
        repaint();
        unlock.set(true);
    }

    @Subscribe
    public void onEvent(UndoEvent e) {
        if (!unlock.compareAndSet(true, false)) {
            System.out.println("轮到你下时才能悔棋");
            return;
        }
        if (history.size() >= 2 && step >= 2) {
            //取消电脑最后1步
            Point p = history.remove(history.size() - 1);
            matrix[p.i][p.j] = EMPTY;

            //取消你最后1步
            p = history.remove(history.size() - 1);
            matrix[p.i][p.j] = EMPTY;

            step -= 2;

            repaint();
        }
        unlock.set(true);
    }

    /**
     * 防止闪烁
     *
     * @param g
     */
    @Override
    public void update(Graphics g) {
        Image buffer = createImage(this.getWidth(), this.getHeight());
        Graphics bg = buffer.getGraphics();
        paint(bg);
        bg.dispose();
        g.drawImage(buffer, 0, 0, this);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2 = (Graphics2D) g;

        //绘制棋盘
        g2.drawImage(BACKGROUND_IMG, 0, 0, getWidth(), getHeight(), this);

        //绘制棋盘
        g2.setStroke(new BasicStroke(4.0f));
        g2.drawRect(MARGIN_BORDER, MARGIN_BORDER, LINE_SPACING * 14 + MARGIN_RECT * 2, LINE_SPACING * 14 + MARGIN_RECT * 2);

        g2.setStroke(new BasicStroke(2.0f));
        String[] columns = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O"};
        String[] rows = {"15", "14", "13", "12", "11", "10", "9", "8", "7", "6", "5", "4", "3", "2", "1"};
        Font font = new Font("Tahoma", Font.BOLD, 14);
        g2.setFont(font);
        FontMetrics fontMetrics = g2.getFontMetrics(font);

        for (int i = 0; i < 15; i++) {
            g2.drawLine(MARGIN, MARGIN + LINE_SPACING * i, MARGIN + 14 * LINE_SPACING, MARGIN + LINE_SPACING * i);//水平画线: 三
            g2.drawLine(MARGIN + LINE_SPACING * i, MARGIN, MARGIN + LINE_SPACING * i, MARGIN + 14 * LINE_SPACING);//垂直画线: 川

            int textWidth = fontMetrics.stringWidth(columns[i]);
            int textHeight = fontMetrics.getHeight();
            g2.drawString(columns[i], MARGIN + LINE_SPACING * i - textWidth / 2, MARGIN_BORDER - textHeight / 2 + 5);

            textWidth = fontMetrics.stringWidth(rows[i]);
            g2.drawString(rows[i], MARGIN_BORDER - textWidth / 2 - 15, MARGIN + LINE_SPACING * i + textHeight / 2);
        }

        final int FLAG_SIZE = 8;
        Point[] points = {new Point(3, 3), new Point(11, 3), new Point(7, 7), new Point(3, 11), new Point(11, 11)};
        for (Point p : points) {
            g2.fillRect(MARGIN + LINE_SPACING * p.i - FLAG_SIZE / 2, MARGIN + LINE_SPACING * p.j - FLAG_SIZE / 2, FLAG_SIZE, FLAG_SIZE);
        }

        //绘制棋子
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (matrix[i][j] != EMPTY) {
                    paintChessman(new Point(i, j), matrix[i][j], g);
                }
            }
        }
        //绘制标记
        if (phase != PHASE_GAME_OVER && !history.isEmpty()) {
            Point lastPoint = history.get(history.size() - 1);
            paintLastStepSignal(lastPoint, g);
        }

        //绘制双方下棋的轨迹
        if (phase == PHASE_GAME_OVER && !history.isEmpty()) {
            g.setColor(Color.RED);
            int index = 0;
            int textHeight = fontMetrics.getHeight();
            for (Point p : history) {
                String text = String.valueOf(++index);
                int textWidth = fontMetrics.stringWidth(text);
                g2.drawString(text, MARGIN + LINE_SPACING * p.i - textWidth / 2, MARGIN + LINE_SPACING * p.j + textHeight / 2 - 5);
            }
        }
    }

    private void paintChessman(Point point, byte color, Graphics g) {
        Image chessmanImg = (color == BLACK ? BLACK_CHESSMAN_IMG : WHITE_CHESSMAN_IMG);
        int x = MARGIN + point.i * LINE_SPACING - CHESS_SIZE / 2;
        int y = MARGIN + point.j * LINE_SPACING - CHESS_SIZE / 2;
        g.drawImage(chessmanImg, x, y, CHESS_SIZE, CHESS_SIZE, this);
    }

    private void paintLastStepSignal(Point point, Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2.0f));
        g2.setColor(Color.RED);
        g2.drawLine(MARGIN + LINE_SPACING * point.i - 5, MARGIN + LINE_SPACING * point.j, MARGIN + LINE_SPACING * point.i + 5, MARGIN + LINE_SPACING * point.j);
        g2.drawLine(MARGIN + LINE_SPACING * point.i, MARGIN + LINE_SPACING * point.j - 5, MARGIN + LINE_SPACING * point.i, MARGIN + LINE_SPACING * point.j + 5);
    }

}
