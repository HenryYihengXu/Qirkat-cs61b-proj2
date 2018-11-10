package qirkat;

import ucb.gui2.Pad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.util.Observer;
import java.util.Observable;

import java.awt.event.MouseEvent;

import static qirkat.Move.SIDE;
import static qirkat.PieceColor.*;

/** Widget for displaying a Qirkat board.
 *  @author Henry Xu
 */
class BoardWidget extends Pad implements Observer {

    /** Length of side of one square, in pixels. */
    static final int SQDIM = 50;
    /** Number of squares on a side. */
    static final int SIDE = Move.SIDE;
    /** Radius of circle representing a piece. */
    static final int PIECE_RADIUS = 15;

    /** Color of white pieces. */
    private static final Color WHITE_COLOR = Color.WHITE;
    /** Color of "phantom" white pieces. */
    /** Color of black pieces. */
    private static final Color BLACK_COLOR = Color.BLACK;
    /** Color of painted lines. */
    private static final Color LINE_COLOR = Color.BLACK;
    /** Color of blank squares. */
    private static final Color BLANK_COLOR = new Color(250, 200, 10);

    /** Stroke for lines.. */
    private static final BasicStroke LINE_STROKE = new BasicStroke(1.0f);

    /** Stroke for outlining pieces. */
    private static final BasicStroke OUTLINE_STROKE = LINE_STROKE;

    /** Model being displayed. */
    private static Board _model;

    /** A new widget displaying MODEL. */
    BoardWidget(Board model) {
        _model = model;
        setMouseHandler("click", this::readMove);
        _model.addObserver(this);
        _dim = SQDIM * SIDE;
        setPreferredSize(_dim, _dim);
    }

    /** Indicate that the squares indicated by MOV are the currently selected
     *  squares for a pending move. */
    void indicateMove(Move mov) {
        _selectedMove = mov;
        repaint();
    }

    /** Indicate the squares indicated by MOV are not the currently selected
     *  squares for a pending move any more. */
    void cancelMove() {
        _selectedMove = null;
        repaint();
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        g.setColor(BLANK_COLOR);
        g.fillRect(0, 0, _dim, _dim);
        paintLines(g);
        paintPieces(g);
    }

    /** Paint lines on the widget.
     *  @param g */
    private void paintLines(Graphics2D g) {
        g.setColor(BLACK_COLOR);
        for (int col = SQDIM / 2; col < _dim; col += SQDIM) {
            g.drawLine(col, SQDIM / 2, col, _dim - SQDIM / 2);
        }
        for (int row = SQDIM / 2; row < _dim; row += SQDIM) {
            g.drawLine(SQDIM / 2, row, _dim - SQDIM / 2, row);
        }
        int low = SQDIM / 2, mid = 5 * SQDIM / 2, high = _dim - SQDIM / 2;
        g.drawLine(mid, low, low, mid);
        g.drawLine(high, low, low, high);
        g.drawLine(high, mid, mid, high);
        g.drawLine(low, mid, mid, high);
        g.drawLine(low, low, high, high);
        g.drawLine(mid, low, high, mid);
    }

    /** Paint pieces on the widget.
     *  @param g */
    private void paintPieces(Graphics2D g) {
        int margin = SQDIM / 2 - PIECE_RADIUS;
        for (int i = SIDE * SIDE - 1; i >= 0; i--) {
            PieceColor p = _model.get(i);
            if (p == BLACK) {
                g.setColor(BLACK_COLOR);
                g.fillOval((i % SIDE) * SQDIM + margin,
                        (SIDE - i / SIDE - 1) * SQDIM + margin,
                        2 * PIECE_RADIUS, 2 * PIECE_RADIUS);
            } else if (p == WHITE) {
                g.setColor(WHITE_COLOR);
                g.fillOval((i % SIDE) * SQDIM + margin,
                        (SIDE - i / SIDE - 1) * SQDIM + margin,
                        2 * PIECE_RADIUS, 2 * PIECE_RADIUS);
            } else {
                g.setColor(new Color(10 * 10, 2 * 10 * 10, 2 * 10 * 10));
                g.fillOval((i % SIDE) * SQDIM + 2 * 10,
                        (SIDE - i / SIDE - 1) * SQDIM + 2 * 10,
                        2 * 5, 2 * 5);
            }
        }
        if (_selectedMove != null) {
            int index = _selectedMove.fromIndex();
            if (_model.get(index) == BLACK) {
                g.setColor(BLACK_COLOR);
                g.fillOval((index % SIDE) * SQDIM + margin - 5,
                        (SIDE - index / SIDE - 1) * SQDIM + margin - 5,
                        2 * PIECE_RADIUS + 10, 2 * PIECE_RADIUS + 10);
            } else {
                g.setColor(WHITE_COLOR);
                g.fillOval((index % SIDE) * SQDIM + margin - 5,
                        (SIDE - index / SIDE - 1) * SQDIM + margin - 5,
                        2 * PIECE_RADIUS + 10, 2 * PIECE_RADIUS + 10);
            }

        }
    }
    /** Notify observers of mouse's current position from click event WHERE. */
    private void readMove(String unused, MouseEvent where) {
        int x = where.getX(), y = where.getY();
        char mouseCol, mouseRow;
        if (where.getButton() == MouseEvent.BUTTON1) {
            mouseCol = (char) (x / SQDIM + 'a');
            mouseRow = (char) ((SQDIM * SIDE - y) / SQDIM + '1');
            if (mouseCol >= 'a' && mouseCol <= 'g'
                    && mouseRow >= '1' && mouseRow <= '7') {
                setChanged();
                notifyObservers("" + mouseCol + mouseRow);
            }
        }
    }

    @Override
    public synchronized void update(Observable model, Object arg) {
        repaint();
    }


    /** Dimension of current drawing surface in pixels. */
    private int _dim;

    /** A partial Move indicating selected squares. */
    private Move _selectedMove;
}
