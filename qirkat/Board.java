package qirkat;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Observable;
import java.util.Observer;

import static java.lang.Math.abs;

import static qirkat.PieceColor.*;
import static qirkat.Move.*;

/** A Qirkat board.   The squares are labeled by column (a char value between
 *  'a' and 'e') and row (a char value between '1' and '5'.
 *
 *  For some purposes, it is useful to refer to squares using a single
 *  integer, which we call its "linearized index".  This is simply the
 *  number of the square in row-major order (with row 0 being the bottom row)
 *  counting from 0).
 *
 *  Moves on this board are denoted by Moves.
 *  @author Henry Xu
 */
class Board extends Observable {

    /** A new, cleared board at the start of the game. */
    Board() {
        _board = new PieceColor[SIDE * SIDE];
        clear();
    }

    /** A copy of B. */
    Board(Board b) {
        _board = new PieceColor[SIDE * SIDE];
        internalCopy(b);
    }

    /** Return a constant view of me (allows any access method, but no
     *  method that modifies it). */
    Board constantView() {
        return this.new ConstantBoard();
    }

    /** Clear me to my starting state, with pieces in their initial
     *  positions. */
    void clear() {
        _whoseMove = WHITE;
        _gameOver = false;

        for (int i = 0; i < SIDE * SIDE; i++) {
            _board[i] = ORIGIN[i];
        }

        resetMoveList();
        setChanged();
        notifyObservers();
    }

    /** Copy B into me. */
    void copy(Board b) {
        internalCopy(b);
    }

    /** Copy B into me. */
    private void internalCopy(Board b) {
        _gameOver = b.gameOver();
        _whoseMove = b.whoseMove();
        for (int i = 0; i < SIDE * SIDE; i++) {
            _board[i] = b.get(i);
        }
        for (Move mov: b.moveList) {
            moveList.add(mov);
        }
    }

    /** Set my contents as defined by STR.  STR consists of 25 characters,
     *  each of which is b, w, or -, optionally interspersed with whitespace.
     *  These give the contents of the Board in row-major order, starting
     *  with the bottom row (row 1) and left column (column a). All squares
     *  are initialized to allow horizontal movement in either direction.
     *  NEXTMOVE indicates whose move it is.
     */
    void setPieces(String str, PieceColor nextMove) {
        if (nextMove == EMPTY || nextMove == null) {
            throw new IllegalArgumentException("bad player color");
        }
        str = str.replaceAll("\\s", "");
        if (!str.matches("[bw-]{25}")) {
            throw new IllegalArgumentException("bad board description");
        }

        _whoseMove = nextMove;

        for (int k = 0; k < str.length(); k += 1) {
            switch (str.charAt(k)) {
            case '-':
                set(k, EMPTY);
                break;
            case 'b': case 'B':
                set(k, BLACK);
                break;
            case 'w': case 'W':
                set(k, WHITE);
                break;
            default:
                break;
            }
        }

        if (isMove()) {
            _gameOver = false;
        } else {
            _gameOver = true;
        }

        setChanged();
        notifyObservers();
    }

    /** Return true iff the game is over: i.e., if the current player has
     *  no moves. */
    boolean gameOver() {
        return _gameOver;
    }

    /** Return the current contents of square C R, where 'a' <= C <= 'e',
     *  and '1' <= R <= '5'.  */
    PieceColor get(char c, char r) {
        assert validSquare(c, r);
        return get(index(c, r));
    }

    /** Return the current contents of the square at linearized index K. */
    PieceColor get(int k) {
        assert validSquare(k);
        return _board[k];
    }

    /** Set get(C, R) to V, where 'a' <= C <= 'e', and
     *  '1' <= R <= '5'. */
    private void set(char c, char r, PieceColor v) {
        assert validSquare(c, r);
        set(index(c, r), v);
    }

    /** Set get(K) to V, where K is the linearized index of a square. */
    private void set(int k, PieceColor v) {
        assert validSquare(k);
        _board[k] = v;
    }

    /** Return true iff MOV is legal on the current board. */
    boolean legalMove(Move mov) {
        if (mov.isJump()) {
            return checkJump(mov, false);
        } else {
            if (jumpPossible()) {
                return false;
            }
            if (_whoseMove == WHITE && mov.row0() == '5') {
                return false;
            }
            if (_whoseMove == BLACK && mov.row0() == '1') {
                return false;
            }
            if (_board[mov.fromIndex()] != _whoseMove) {
                return false;
            }
            if (_board[mov.toIndex()] != EMPTY) {
                return false;
            }
            if (_whoseMove == WHITE && mov.row1() < mov.row0()) {
                return false;
            }
            if (_whoseMove == BLACK && mov.row1() > mov.row0()) {
                return false;
            }
            if (abs(mov.col0() - mov.col1()) > 2
                    || abs(mov.row0() - mov.row1()) > 2) {
                return false;
            }
            if (abs(mov.col0() - mov.col1()) + abs(mov.row0()
                    - mov.row1()) == 3) {
                return false;
            }
            if (mov.row0() != mov.row1() && mov.col0() != mov.col1()
                    && mov.fromIndex() % 2 == 1) {
                return false;
            }
            for (int i = moveList.size() - 1; i >= 0; i--) {
                Move prevMove = moveList.get(i);
                if (mov.fromIndex() == prevMove.fromIndex()
                        || (mov.fromIndex() == prevMove.toIndex()
                        && mov.toIndex() != prevMove.fromIndex())) {
                    return true;
                }
                if (mov.fromIndex() == prevMove.toIndex()
                        && mov.toIndex() == prevMove.fromIndex()) {
                    return false;
                }
            }
        }
        return true;
    }

    /** A version for AI to speed up.
     *  @param  mov the move to be checked
     *  @return  */
    boolean legalMoveAI(Move mov) {
        if (mov.isJump()) {
            return checkJumpAI(mov, false);
        } else {
            if (_whoseMove == WHITE && mov.row0() == '5') {
                return false;
            }
            if (_whoseMove == BLACK && mov.row0() == '1') {
                return false;
            }
            if (_board[mov.fromIndex()] != _whoseMove) {
                return false;
            }
            if (_board[mov.toIndex()] != EMPTY) {
                return false;
            }
            if (_whoseMove == WHITE && mov.row1() < mov.row0()) {
                return false;
            }
            if (_whoseMove == BLACK && mov.row1() > mov.row0()) {
                return false;
            }
            if (abs(mov.col0() - mov.col1()) > 2
                    || abs(mov.row0() - mov.row1()) > 2) {
                return false;
            }
            if (abs(mov.col0() - mov.col1()) + abs(mov.row0()
                    - mov.row1()) == 3) {
                return false;
            }
            if (mov.row0() != mov.row1() && mov.col0() != mov.col1()
                    && mov.fromIndex() % 2 == 1) {
                return false;
            }
            for (int i = moveList.size() - 1; i >= 0; i--) {
                Move prevMove = moveList.get(i);
                if (mov.fromIndex() == prevMove.fromIndex()
                        || (mov.fromIndex() == prevMove.toIndex()
                        && mov.toIndex() != prevMove.fromIndex())) {
                    return true;
                }
                if (mov.fromIndex() == prevMove.toIndex()
                        && mov.toIndex() == prevMove.fromIndex()) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Return a list of all legal moves from the current position. */
    ArrayList<Move> getMoves() {
        ArrayList<Move> result = new ArrayList<>();
        getMoves(result);
        return result;
    }

    /** Add all legal moves from the current position to MOVES. */
    void getMoves(ArrayList<Move> moves) {
        if (gameOver()) {
            return;
        }
        if (jumpPossible()) {
            for (int k = 0; k <= MAX_INDEX; k += 1) {
                getJumps(moves, k);
            }
        } else {
            for (int k = 0; k <= MAX_INDEX; k += 1) {
                getMoves(moves, k);
            }
        }
    }

    /** Add all legal non-capturing moves from the position
     *  with linearized index K to MOVES. */
    private void getMoves(ArrayList<Move> moves, int k) {
        char col0 = Move.col(k);
        char row0 = Move.row(k);
        for (int i = -1; i <= 1; i++) {
            char row1 = Move.row(k + 5 * i);
            for (int j = -1; j <= 1; j++) {
                char col1 = Move.col(k + j);
                if (Move.validSquare(col1, row1)) {
                    Move mov = Move.move(col0, row0, col1, row1);
                    if (legalMoveAI(mov)) {
                        moves.add(mov);
                    }
                }
            }
        }
    }


    /** Add all legal captures from the position with linearized index K
     *  to MOVES. */
    private void getJumps(ArrayList<Move> moves, int k) {
        ArrayList<Move> jumps = getJumps(k);
        for (Move mov: jumps) {

            moves.add(mov);


        }
    }

    /** Get all the multiple jumps.
     *  @param k the from index of possible moves
     *  @return */
    private ArrayList<Move> getJumps(int k) {
        ArrayList<Move> jumps = new ArrayList<>();
        char col0 = Move.col(k);
        char row0 = Move.row(k);
        for (int i = -1; i <= 1; i++) {
            char row1 = Move.row(k + 5 * 2 * i);
            for (int j = -1; j <= 1; j++) {
                char col1 = Move.col(k + 2 * j);
                if (Move.validSquare(col1, row1)) {
                    Move mov = Move.move(col0, row0, col1, row1);
                    if (checkJumpAI(mov, true)) {

                        makeMove(mov);
                        _whoseMove = _whoseMove.opposite();
                        ArrayList<Move> next = getJumps(Move.index(col1, row1));
                        _whoseMove = _whoseMove.opposite();
                        undo();


                        if (next.isEmpty()) {
                            jumps.add(mov);
                        } else {
                            for (Move nextMov: next) {
                                jumps.add(Move.move(mov, nextMov));
                            }
                        }

                    }
                }
            }
        }
        return jumps;
    }

    /** Return true iff MOV is a valid jump sequence on the current board.
     *  MOV must be a jump or null.  If ALLOWPARTIAL, allow jumps that
     *  could be continued and are valid as far as they go.  */
    boolean checkJump(Move mov, boolean allowPartial) {
        if (mov == null) {
            return false;
        }
        if (!mov.isJump()) {
            return false;
        }
        if (_board[mov.fromIndex()] != _whoseMove) {
            return false;
        }
        if (_board[mov.toIndex()] != EMPTY) {
            return false;
        }
        if ((_board[mov.jumpedIndex()] == _whoseMove
                || _board[mov.jumpedIndex()] == EMPTY)) {
            return false;
        }
        if (abs(mov.col0() - mov.col1()) > 2
                || abs(mov.row0() - mov.row1()) > 2) {
            return false;
        }
        if (abs(mov.col0() - mov.col1()) + abs(mov.row0() - mov.row1()) == 3) {
            return false;
        }
        if (mov.row0() != mov.row1() && mov.col0() != mov.col1()
                && mov.fromIndex() % 2 == 1) {
            return false;
        }
        mov = mov.jumpTail();
        while (mov != null) {
            if (!mov.isJump()) {
                return false;
            }
            if (_board[mov.toIndex()] != EMPTY) {
                return false;
            }
            if ((_board[mov.jumpedIndex()] == _whoseMove
                    || _board[mov.jumpedIndex()] == EMPTY)) {
                return false;
            }
            if (abs(mov.col0() - mov.col1()) > 2
                    || abs(mov.row0() - mov.row1()) > 2) {
                return false;
            }
            if (abs(mov.col0() - mov.col1())
                    + abs(mov.row0() - mov.row1()) == 3) {
                return false;
            }
            if (!mov.isVestigial()
                    && (abs(mov.fromIndex() - mov.toIndex()) % 6 == 0
                    || abs(mov.fromIndex() - mov.toIndex()) % 4 == 0)
                    && mov.fromIndex() % 2 == 1) {
                return false;
            }
            mov = mov.jumpTail();
        }
        return true;
    }

    /** A version of checkJump for AI to speed up.
     *  @param mov the move to be checked
     *  @param allowPartial control if a move have to be complete
     *  @return  */
    boolean checkJumpAI(Move mov, boolean allowPartial) {
        if (_board[mov.fromIndex()] != _whoseMove) {
            return false;
        }
        if (_board[mov.toIndex()] != EMPTY) {
            return false;
        }
        if ((_board[mov.jumpedIndex()] == _whoseMove
                || _board[mov.jumpedIndex()] == EMPTY)) {
            return false;
        }
        if (abs(mov.col0() - mov.col1()) > 2
                || abs(mov.row0() - mov.row1()) > 2) {
            return false;
        }
        if (abs(mov.col0() - mov.col1()) + abs(mov.row0() - mov.row1()) == 3) {
            return false;
        }
        if (mov.row0() != mov.row1() && mov.col0() != mov.col1()
                && mov.fromIndex() % 2 == 1) {
            return false;
        }
        return true;
    }

    /** Return true iff a jump is possible for a piece at position C R. */
    boolean jumpPossible(char c, char r) {
        return jumpPossible(index(c, r));
    }

    /** Return true iff a jump is possible for a piece at position with
     *  linearized index K. */
    boolean jumpPossible(int k) {
        char col0 = Move.col(k);
        char row0 = Move.row(k);
        for (int i = -1; i <= 1; i++) {
            char row1 = Move.row(k + 5 * 2 * i);
            for (int j = -1; j <= 1; j++) {
                char col1 = Move.col(k + 2 * j);
                if (Move.validSquare(col1, row1)) {
                    Move mov = Move.move(col0, row0, col1, row1);
                    if (checkJumpAI(mov, true)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Return true iff a jump is possible from the current board. */
    boolean jumpPossible() {
        for (int k = 0; k <= MAX_INDEX; k += 1) {
            if (jumpPossible(k)) {
                return true;
            }
        }
        return false;
    }

    /** Check if there is any possible move at k.
     *  @param k check if there are any move from the kth position
     *  @return  */
    boolean movePossible(int k) {
        char col0 = Move.col(k);
        char row0 = Move.row(k);
        for (int i = -1; i <= 1; i++) {
            char row1 = Move.row(k + 5 * i);
            for (int j = -1; j <= 1; j++) {
                char col1 = Move.col(k + j);
                if (Move.validSquare(col1, row1)) {
                    Move mov = Move.move(col0, row0, col1, row1);
                    if (legalMoveAI(mov)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Check if there is any possible move on the whole board.
     *  @return  */
    boolean movePossible() {
        for (int k = 0; k <= MAX_INDEX; k += 1) {
            if (movePossible(k)) {
                return true;
            }
        }
        return false;
    }
    /** Return the color of the player who has the next move.  The
     *  value is arbitrary if gameOver(). */
    PieceColor whoseMove() {
        return _whoseMove;
    }

    /** Change the color of the player who has the next move.
     *  @param v set _whoseMove to be v */
    void setWhoseMove(PieceColor v) {
        _whoseMove = v;
    }

    /** Perform the move C0R0-C1R1. Assumes that legalMove(C0, R0, C1, R1). */
    void makeMove(char c0, char r0, char c1, char r1) {
        makeMove(Move.move(c0, r0, c1, r1, null));
    }

    /** Make the multi-jump C0 R0-C1 R1..., where NEXT is C1R1....
     *  Assumes the result is legal. */
    void makeMove(char c0, char r0, char c1, char r1, Move next) {
        makeMove(Move.move(c0, r0, c1, r1, next));
    }

    /** Make the Move MOV on this Board, assuming it is legal. */
    void makeMove(Move mov) {

        moveList.add(mov);
        if (!mov.isJump()) {
            set(mov.col0(), mov.row0(), EMPTY);
            set(mov.col1(), mov.row1(), _whoseMove);
        } else {
            while (mov != null) {
                set(mov.col0(), mov.row0(), EMPTY);
                set(mov.col1(), mov.row1(), _whoseMove);
                set(mov.jumpedIndex(), EMPTY);
                mov = mov.jumpTail();
            }
        }
        _whoseMove = _whoseMove.opposite();
        if (!isMove()) {
            _gameOver = true;
        }


        setChanged();
        notifyObservers();
    }

    /** Undo the last move, if any. */
    void undo() {
        Move mov = moveList.remove(moveList.size() - 1);
        ArrayList<Move> moves = new ArrayList<>();
        while (mov != null) {
            moves.add(mov);
            mov = mov.jumpTail();
        }
        while (moves.size() > 0) {
            undo(moves.remove(moves.size() - 1));
        }
        _whoseMove = _whoseMove.opposite();
        _gameOver = false;

        setChanged();
        notifyObservers();
    }

    /** Helper function. Undo the MOV. */
    void undo(Move mov) {
        if (!mov.isJump()) {
            set(mov.col0(), mov.row0(), _whoseMove.opposite());
            set(mov.col1(), mov.row1(), EMPTY);
        } else {
            set(mov.col0(), mov.row0(), _whoseMove.opposite());
            set(mov.jumpedIndex(), _whoseMove);
            set(mov.col1(), mov.row1(), EMPTY);
        }
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /** Return a text depiction of the board.  If LEGEND, supply row and
     *  column numbers around the edges. */
    String toString(boolean legend) {
        Formatter out = new Formatter();
        if (!legend) {
            for (int i = 4; i > 0; i--) {
                out.format(" ");
                for (int j = 0; j < 5; j++) {
                    out.format(" %s", _board[i * 5 + j].shortName());
                }
                out.format("\n");
            }
            out.format(" ");
            for (int j = 0; j < 5; j++) {
                out.format(" %s", _board[j].shortName());
            }
        } else {
            for (int i = 4; i >= 0; i--) {
                out.format("  %d", i + 1);
                for (int j = 0; j < 5; j++) {
                    out.format(" %s", _board[i * SIDE + j].shortName());
                }
                out.format("\n");
            }
            out.format("    a b c d e");
        }
        return out.toString();
    }

    /** Return true iff there is a move for the current player. */
    private boolean isMove() {
        return jumpPossible() || movePossible();
    }


    /** Player that is on move. */
    private PieceColor _whoseMove;

    /** Player that is on move. */
    static final PieceColor[] ORIGIN = {
        WHITE, WHITE, WHITE, WHITE, WHITE,
        WHITE, WHITE, WHITE, WHITE, WHITE,
        BLACK, BLACK, EMPTY, WHITE, WHITE,
        BLACK, BLACK, BLACK, BLACK, BLACK,
        BLACK, BLACK, BLACK, BLACK, BLACK,
    };

    /** Player that is on move. */
    private PieceColor[] _board;

    /** Set true when game ends. */
    private boolean _gameOver;

    /** Convenience value giving values of pieces at each ordinal position. */
    static final PieceColor[] PIECE_VALUES = PieceColor.values();

    /** One cannot create arrays of ArrayList<Move>, so we introduce
     *  a specialized private list type for this purpose. */
    private static class MoveList extends ArrayList<Move> {
    }

    /** List of all the previous moves you have done. */
    private MoveList moveList = new MoveList();

    /** Clean moveList. */
    public void resetMoveList() {
        moveList.clear();
    }

    /** Method to compare this board to another board. */
    @Override
    public boolean equals(Object b) {
        if (!(b instanceof Board)) {
            return false;
        }
        if (((Board) b).whoseMove() != _whoseMove
                || ((Board) b).gameOver() != _gameOver) {
            return false;
        }
        for (int i = 0; i < SIDE * SIDE; i++) {
            if (((Board) b).get(i) != _board[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /** A read-only view of a Board. */
    private class ConstantBoard extends Board implements Observer {
        /** A constant view of this Board. */
        ConstantBoard() {
            super(Board.this);
            Board.this.addObserver(this);
        }

        @Override
        void copy(Board b) {
            assert false;
        }

        @Override
        void clear() {
            assert false;
        }

        @Override
        void makeMove(Move move) {
            assert false;
        }

        /** Undo the last move. */
        @Override
        void undo() {
            assert false;
        }

        @Override
        public void update(Observable obs, Object arg) {
            super.copy((Board) obs);
            setChanged();
            notifyObservers(arg);
        }
    }
}
