package fir;

import fir.vo.Shape;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by kongzheng on 16/9/7.
 */
public class ShapeAnalyzerTest {

    @Test
    public void testAnalyze() throws Exception {
        final byte B = ChessBoard.BLACK;
        final byte W = ChessBoard.WHITE;
        final byte E = ChessBoard.EMPTY;
        ShapeAnalyzer analyzer = new ShapeAnalyzer();
        Assert.assertEquals(Shape.five, analyzer.analyze(new byte[]{E, B, B, B, B, B, E}, 2, B));
        Assert.assertEquals(Shape.four, analyzer.analyze(new byte[]{E, B, E, B, B, B, B, E}, 2, B));
        Assert.assertEquals(Shape.four, analyzer.analyze(new byte[]{E, B, B, B, B, E}, 2, B));
        Assert.assertEquals(Shape.blockedFour, analyzer.analyze(new byte[]{W, B, B, B, B, E}, 2, B));
        Assert.assertEquals(Shape.three, analyzer.analyze(new byte[]{W, B, E, B, B, B, E}, 2, B));
        Assert.assertEquals(Shape.three, analyzer.analyze(new byte[]{W, B, B, E, B, B, E}, 2, B));
        Assert.assertEquals(Shape.three, analyzer.analyze(new byte[]{W, B, B, B, E, B, E}, 2, B));
        Assert.assertEquals(Shape.three, analyzer.analyze(new byte[]{E, E, B, B, B, E, E}, 2, B));
    }
}