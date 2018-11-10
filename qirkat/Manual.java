package qirkat;


import static qirkat.PieceColor.*;
import static qirkat.Command.Type.*;

/** A Player that receives its moves from its Game's getMoveCmnd method.
 *  @author Henry Xu
 */
class Manual extends Player {

    /** A Player that will play MYCOLOR on GAME, taking its moves from
     *  GAME. */
    Manual(Game game, PieceColor myColor) {
        super(game, myColor);
        _prompt = myColor + ": ";
    }

    @Override
    Move myMove() {
        while (true) {
            Command cmnd = game().getMoveCmnd(_prompt);
            if (cmnd == null) {
                return null;
            } else {
                Move mov = Move.parseMove(cmnd.operands()[0]);
                if (board().legalMove(mov)) {
                    return mov;
                }
            }
        }


    }

    /** Identifies the player serving as a source of input commands. */
    private String _prompt;
}

