package fir;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import fir.evt.ResetSituationEvent;
import fir.evt.UndoEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Executors;

/**
 * Created by kongzheng on 16/8/26.
 */
public class MainFrame extends JFrame {

    private ChessBoard chessBoard;
    private EventBus bus;

    public ChessBoard getChessBoard() {
        return chessBoard;
    }

    public void setBus(EventBus bus) {
        this.bus = bus;
    }

    public MainFrame() {
        initComponents();
        initEventBus();
    }

    private void initEventBus() {
        //异步事件总线,可以防止AI陷入长时间思考时卡住UI界面
        EventBus bus = new AsyncEventBus("business-thread", Executors.newFixedThreadPool(3));

        setBus(bus);
        bus.register(this);

        getChessBoard().setBus(bus);
        bus.register(getChessBoard());

        Umpire umpire = new Umpire();
        umpire.setBus(bus);
        bus.register(umpire);

        Robot robot = new Robot();
//        Robot robot = new Robot();
        robot.setBus(bus);
        bus.register(robot);

        SoundPlayer soundPlayer = new SoundPlayer();
        bus.register(soundPlayer);
    }

    private void initComponents() {
        int width = 922;
        int height = 722;
        setResizable(false);
        setTitle("五子棋");
        setSize(width, height);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); //设置窗口居中
        setLayout(new BorderLayout());

        //棋盘区域
        chessBoard = new ChessBoard();
        chessBoard.setPreferredSize(new Dimension(width - 200, height - getInsets().top));
        getContentPane().add(chessBoard, BorderLayout.WEST);

        //操作面板区域
        JPanel jPanel = new JPanel();
        jPanel.setPreferredSize(new Dimension(200, height - getInsets().top));
        JButton undoButton = new JButton("悔棋");
        JButton restartButton = new JButton("重玩");
        jPanel.add(undoButton);
        jPanel.add(restartButton);
        getContentPane().add(jPanel, BorderLayout.EAST);

        pack();
        setVisible(true);

        final JFrame self = this;

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                self.setTitle("五子棋: " + e.getX() + "," + e.getY());
            }
        });

        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int flag = JOptionPane.showConfirmDialog(self, "是否重新开始?", "游戏结束", JOptionPane.OK_CANCEL_OPTION);
                if (flag == JOptionPane.OK_OPTION) {
                    bus.post(new ResetSituationEvent());
                }
            }
        });

        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bus.post(new UndoEvent());
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame frame = new MainFrame();
            }
        });
    }
}
