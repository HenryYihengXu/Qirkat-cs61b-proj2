package qirkat;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class MoreBoardTests {
    private final char[][] boardRepr = new char[][]{
        {'b', 'b', 'b', 'b', 'b'},
        {'b', 'b', 'b', 'b', 'b'},
        {'b', 'b', '-', 'w', 'w'},
        {'w', 'w', 'w', 'w', 'w'},
        {'w', 'w', 'w', 'w', 'w'}
    };

    private static final String INIT_BOARD_EDGED =
            "  5 b b b b b\n  4 b b b b b\n  3 b b - w w\n"
                    + "  2 w w w w w\n  1 w w w w w\n    a b c d e";

    private final PieceColor currMove = PieceColor.WHITE;

    private String getInitialRepresentation() {
        StringBuilder sb = new StringBuilder();
        sb.append("  ");
        for (int i = boardRepr.length - 1; i >= 0; i--) {
            for (int j = 0; j < boardRepr[0].length;
                 j++) {
                sb.append(boardRepr[i][j] + " ");
            }
            sb.deleteCharAt(sb.length() - 1);
            if (i != 0) {
                sb.append("\n  ");
            }
        }
        return sb.toString();
    }

    private Board getBoard() {
        Board b = new Board();
        b.setPieces(getInitialRepresentation(), currMove);
        return b;
    }

    private void resetToInitialState(Board b) {
        b.setPieces(getInitialRepresentation(), currMove);
    }

    @Test
    public void testSomething() {
        Board b = getBoard();

        Move mov1 = Move.move('c', '2', 'c', '3');
        Move mov2 = Move.move('a', '1', 'a', '2');
        Move mov3 = Move.move('c', '3', 'c', '4');
        assert (b.legalMove(mov1));
        assert (!b.legalMove(mov2));
        assert (!b.legalMove(mov3));


        resetToInitialState(b);
        ArrayList<Move> expectedMoves = new ArrayList<>();
        expectedMoves.add(Move.move('b', '2', 'c', '3'));
        expectedMoves.add(Move.move('c', '2', 'c', '3'));
        expectedMoves.add(Move.move('d', '2', 'c', '3'));
        expectedMoves.add(Move.move('d', '3', 'c', '3'));

        assertEquals(expectedMoves, b.getMoves());

        b.makeMove('c', '2', 'c', '3');
        assert b.jumpPossible();



        resetToInitialState(b);
        assertEquals(INIT_BOARD_EDGED,
                b.toString(true));
    }
}
