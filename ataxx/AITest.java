package ataxx;

import org.junit.Test;

public class AITest {

    @Test
    public void testAIStart() {
        Game game;
        GUI display = new GUI("Ataxx!");
        game = new Game(display, display, display, true);

        AI ai = new AI(game, PieceColor.BLUE, 10);
        System.out.println(ai.getMove());
    }
}
