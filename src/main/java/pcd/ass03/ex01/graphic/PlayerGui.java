package pcd.ass03.ex01.graphic;

public interface PlayerGui {
    void launch();
    void loseMatch();
    void finishMatch(String winnerName, boolean youWin);
    void startTurn();
    void startFinalTurnPart();
}
