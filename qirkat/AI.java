package qirkat;

import java.util.ArrayList;

import static qirkat.PieceColor.*;
import static qirkat.Move.*;

/** A Player that computes its own moves.
 *  @author Henry Xu
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 8;
    /** A position magnitude indicating a win (for white if positive, black
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI for GAME that will play MYCOLOR. */
    AI(Game game, PieceColor myColor) {
        super(game, myColor);
    }

    @Override
    Move myMove() {
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();

        if (myColor() == WHITE) {
            System.out.println("White" + " moves " + move.toString() + ".");
        } else {
            System.out.println("Black" + " moves " + move.toString() + ".");
        }


        return move;
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        if (myColor() == WHITE) {
            findMove(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            findMove(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        Move best;
        best = null;
        int bestVal;
        if (depth == 0 || board.gameOver()) {
            return staticScore(board);
        }
        if (sense == 1) {
            bestVal = -INFTY;
            ArrayList<Move> moves = board.getMoves();
            for (Move mov: moves) {
                board.makeMove(mov);
                int response = findMove(board, depth - 1,
                        !saveMove, -1, alpha, beta);
                board.undo();
                if (response >= bestVal) {
                    best = mov;
                    bestVal = response;
                    alpha = Math.max(response, alpha);
                }
                if (beta <= alpha) {
                    break;
                }
            }
        } else {
            bestVal = INFTY;
            ArrayList<Move> moves = board.getMoves();
            for (Move mov: moves) {
                board.makeMove(mov);
                int response = findMove(board, depth - 1,
                        !saveMove, 1, alpha, beta);
                board.undo();
                if (response <= bestVal) {
                    best = mov;
                    bestVal = response;
                    beta = Math.min(response, beta);
                }
                if (beta <= alpha) {
                    break;
                }
            }
        }


        if (saveMove) {
            _lastFoundMove = best;
        }

        return bestVal;
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        if (board.gameOver()) {
            if (board.whoseMove() == WHITE) {
                return -WINNING_VALUE;
            } else {
                return WINNING_VALUE;
            }
        }
        int white = 0, black = 0;
        int whiteMoves = 0, blackMoves = 0;
        int whiteJumps = 0, blackJumps = 0;
        for (int i = 0; i < SIDE * SIDE; i++) {
            if (board.get(i) == WHITE) {
                white += 1;
                whiteMoves += SIDE * SIDE - i;
            }
            if (board.get(i) == BLACK) {
                black += 1;
                blackMoves += i;
            }
        }
        return ((white - black) * 1000) + (whiteMoves - blackMoves) * 100;
    }
}
