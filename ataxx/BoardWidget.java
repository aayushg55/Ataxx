/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import ucb.gui2.Pad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;

import java.awt.event.MouseEvent;

import java.util.concurrent.ArrayBlockingQueue;

import static ataxx.PieceColor.*;
import static ataxx.Utils.*;

/** Widget for displaying an Ataxx board.
 *  @author Aayush Gupta
 */
class BoardWidget extends Pad  {

    /** Length of side of one square, in pixels. */
    static final int SQDIM = 50;
    /** Size of offset required to center pieces. */
    static final int PIECE_OFFSET = 10;
    /** Size of offset required to center pieces. */
    static final int BLOCK_OFFSET = 25;
    /** Number of squares on a side. */
    static final int SIDE = Board.SIDE;
    /** Radius of circle representing a piece. */
    static final int PIECE_RADIUS = 30;
    /** Dimension of a block. */
    static final int BLOCK_WIDTH = 40;
    /** Int conversion of char 'a'. */
    static final int CHAR_A_INT = 97;
    /** Int conversion of char '1'. */
    static final int CHAR_1_INT = 48;
    /** Color of red pieces. */
    private static final Color RED_COLOR = Color.RED;
    /** Color of blue pieces. */
    private static final Color BLUE_COLOR = Color.BLUE;
    /** Color of painted lines. */
    private static final Color LINE_COLOR = Color.BLACK;
    /** Color of blank squares. */
    private static final Color BLANK_COLOR = Color.WHITE;
    /** Color of selected squared. */
    private static final Color SELECTED_COLOR = new Color(150, 150, 150);
    /** Color of blocks. */
    private static final Color BLOCK_COLOR = Color.BLACK;

    /** Stroke for lines. */
    private static final BasicStroke LINE_STROKE = new BasicStroke(1.0f);
    /** Stroke for blocks. */
    private static final BasicStroke BLOCK_STROKE = new BasicStroke(5.0f);

    /** A new widget sending commands resulting from mouse clicks
     *  to COMMANDQUEUE. */
    BoardWidget(ArrayBlockingQueue<String> commandQueue) {
        _commandQueue = commandQueue;
        setMouseHandler("click", this::handleClick);
        _dim = SQDIM * SIDE;
        _blockMode = false;
        setPreferredSize(_dim, _dim);
        setMinimumSize(_dim, _dim);
    }

    /** Indicate that SQ (of the form CR) is selected, or that none is
     *  selected if SQ is null. */
    void selectSquare(String sq) {
        if (sq == null) {
            _selectedCol = _selectedRow = 0;
        } else {
            _selectedCol = sq.charAt(0);
            _selectedRow = sq.charAt(1);
        }
        repaint();
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        g.setColor(BLANK_COLOR);
        g.setStroke(LINE_STROKE);
        g.setColor(LINE_COLOR);
        for (int i = 0; i <= SIDE; i++) {
            g.drawLine(0, SQDIM * i, _dim, SQDIM * i);
        }
        for (int i = 0; i <= SIDE; i++) {
            g.drawLine(SQDIM * i, 0, SQDIM * i, _dim);
        }

        if (_selectedCol != 0 && _selectedRow != 0 && !_blockMode) {
            g.setColor(SELECTED_COLOR);
            int x = (_selectedCol - CHAR_A_INT) * SQDIM;
            int y = (7 - (_selectedRow - CHAR_1_INT)) * SQDIM;
            g.fillRect(x, y, SQDIM, SQDIM);
        }
        PieceColor piece = null;
        for (char c0 = 'a'; c0 <= 'g'; c0++) {
            for (char r0 = '1'; r0 <= '7'; r0++) {
                piece = _model.get(c0, r0);
                int x = (c0 - CHAR_A_INT) * SQDIM;
                int y = (7 - (r0 - CHAR_1_INT)) * SQDIM;
                if (piece.equals(BLOCKED)) {
                    drawBlock(g, x + BLOCK_OFFSET, y + BLOCK_OFFSET);
                } else if (piece.equals(RED) || piece.equals(BLUE)) {
                    switch (piece) {
                    case RED -> g.setColor(RED_COLOR);
                    case BLUE -> g.setColor(BLUE_COLOR);
                    default -> g.setColor(BLANK_COLOR);
                    }
                    g.fillOval(x + PIECE_OFFSET,
                            y + PIECE_OFFSET, PIECE_RADIUS, PIECE_RADIUS);
                }
            }
        }
    }

    /** Draw a block centered at (CX, CY) on G. */
    void drawBlock(Graphics2D g, int cx, int cy) {
        g.setStroke(BLOCK_STROKE);
        g.setColor(BLOCK_COLOR);
        g.drawLine(cx - BLOCK_WIDTH / 2, cy - BLOCK_WIDTH / 2,
                cx + BLOCK_WIDTH / 2, cy + BLOCK_WIDTH / 2);
        g.drawLine(cx - BLOCK_WIDTH / 2, cy + BLOCK_WIDTH / 2,
                cx + BLOCK_WIDTH / 2, cy - BLOCK_WIDTH / 2);
    }

    /** Clear selected block, if any, and turn off block mode. */
    void reset() {
        _selectedRow = _selectedCol = 0;
        setBlockMode(false);
    }

    /** Set block mode on iff ON. */
    void setBlockMode(boolean on) {
        _blockMode = on;
    }

    /** Issue move command indicated by mouse-click event WHERE. */
    private void handleClick(String unused, MouseEvent where) {
        int x = where.getX(), y = where.getY();
        char mouseCol, mouseRow;
        getGraphics().setColor(SELECTED_COLOR);
        getGraphics().fillRect(x, y, x + SQDIM, y + SQDIM);
        if (where.getButton() == MouseEvent.BUTTON1) {
            mouseCol = (char) (x / SQDIM + 'a');
            mouseRow = (char) ((SQDIM * SIDE - y) / SQDIM + '1');
            if (mouseCol >= 'a' && mouseCol <= 'g'
                && mouseRow >= '1' && mouseRow <= '7') {
                if (_blockMode) {
                    _commandQueue.offer("block " + mouseCol + mouseRow);
                    selectSquare("" + mouseCol + mouseRow);
                } else {
                    if (_selectedCol != 0) {
                        if (_model.legalMove(_selectedCol, _selectedRow,
                                mouseCol, mouseRow)) {
                            _commandQueue.offer("" + _selectedCol
                                    + _selectedRow + '-' + mouseCol + mouseRow);
                        }
                        selectSquare("" + mouseCol + mouseRow);
                        _selectedCol = _selectedRow = 0;
                    } else {
                        _selectedCol = mouseCol;
                        _selectedRow = mouseRow;
                    }
                }
            }
        }
        repaint();
    }

    public synchronized void update(Board board) {
        _model = new Board(board);
        repaint();
    }

    /** Dimension of current drawing surface in pixels. */
    private int _dim;

    /** Model being displayed. */
    private static Board _model;

    /** Coordinates of currently selected square, or '\0' if no selection. */
    private char _selectedCol, _selectedRow;

    /** True iff in block mode. */
    private boolean _blockMode;

    /** Destination for commands derived from mouse clicks. */
    private ArrayBlockingQueue<String> _commandQueue;
}
