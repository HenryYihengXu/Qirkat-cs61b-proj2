package qirkat;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/** Tests of the Board class.
 *  @author Henry Xu
 */
public class BoardTest {

    private static final String INIT_BOARD =
        "  b b b b b\n  b b b b b\n  b b - w w\n  w w w w w\n  w w w w w";

    private static final String INIT_BOARD_EDGED =
            "  5 b b b b b\n  4 b b b b b\n  3 b b - w w\n"
                    + "  2 w w w w w\n  1 w w w w w\n    a b c d e";

    private static final String[] GAME1 =
    { "c2-c3", "c4-c2",
      "c1-c3", "a3-c1",
      "c3-a3", "c5-c4",
      "a3-c5-c3",
    };

    private static final String GAME1_BOARD =
        "  b b - b b\n  b - - b b\n  - - w w w\n  w - - w w\n  w w b w w";

    private static void makeMoves(Board b, String[] moves) {
        for (String s : moves) {
            b.makeMove(Move.parseMove(s));
        }
    }

    @Test
    public void testInit1() {
        Board b0 = new Board();
        assertEquals(INIT_BOARD, b0.toString());
        assertEquals(INIT_BOARD_EDGED, b0.toString(true));
    }

    @Test
    public void testLegalMove() {
        Board b0 = new Board();
        Move mov1 = Move.move('c', '2', 'c', '3');
        Move mov2 = Move.move('a', '1', 'a', '2');
        Move mov3 = Move.move('c', '3', 'c', '4');
        assert (b0.legalMove(mov1));
        assert !b0.legalMove(mov2);
        assert (!b0.legalMove(mov3));
    }

    @Test
    public void testGetMoves() {
        Board b0 = new Board();
        ArrayList<Move> expectedMoves = new ArrayList<>();
        expectedMoves.add(Move.move('b', '2', 'c', '3'));
        expectedMoves.add(Move.move('c', '2', 'c', '3'));
        expectedMoves.add(Move.move('d', '2', 'c', '3'));
        expectedMoves.add(Move.move('d', '3', 'c', '3'));

        assertEquals(expectedMoves, b0.getMoves());
    }

    @Test
    public void testMoves1() {
        Board b0 = new Board();
        makeMoves(b0, GAME1);
        assertEquals(GAME1_BOARD, b0.toString());
    }

    @Test
    public void testUndo() {
        Board b0 = new Board();
        Board b1 = new Board(b0);
        makeMoves(b0, GAME1);
        Board b2 = new Board(b0);
        for (int i = 0; i < GAME1.length; i += 1) {
            b0.undo();
        }
        assertEquals("failed to return to start", b1, b0);
        makeMoves(b0, GAME1);
        assertEquals("second pass failed to reach same position", b2, b0);
    }

}
