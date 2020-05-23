package pcd.ass03.ex01.graphic;

import pcd.ass03.ex01.actors.GuiActor;
import pcd.ass03.ex01.messages.Message;
import pcd.ass03.ex01.messages.WinMsg;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class GameGuiImpl implements GameGui {

    /**
     * Start for button button.
     */
    private static String START = "Start";


    /**
     * Stop for button.
     */
    private static String STOP = "Stop";

    /**
     * Reference to GuiActor.
     */
    private final GuiActor guiActor;

    /**
     * ButtonStart or stop.
     */
    private JButton startStopButton;


    /**
     * Range of cypher (M).
     */
    private final JComboBox<Integer> combinationComboBox;


    /**
     * Range of cypher (M).
     */
    private final JComboBox<Integer> playerComboBox;

    /**
     * Label for log.
     */
    private final JLabel logLabel;


    /**
     * Constructor.
     * @param actorGui reference to his actor
     */
    GameGuiImpl(final GuiActor actorGui) {
        this.guiActor = actorGui;

        final Integer[] depthValues = new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
        combinationComboBox = new JComboBox<>(depthValues);

        this.playerComboBox = new JComboBox<>(depthValues);

        this.startStopButton = new JButton(START);

        this.logLabel = new JLabel("You can start the game!");
        this.logLabel.setPreferredSize(new Dimension(500, 30));

        this.launch();
    }


    private void launch() {
        JFrame mainFrame = new JFrame();

        mainFrame.setTitle("PCD - Assignment 03 - Maldini, Gorini, Angelini - MasterMind");
        mainFrame.setResizable(false);

        JPanel generalPanel = new JPanel();
        generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.PAGE_AXIS));


        final JPanel playersPanel = new JPanel();
        playersPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        playersPanel.add(new JLabel("Number of Player: "));
        playersPanel.add(playerComboBox);
        generalPanel.add(playersPanel);

        final JPanel combinationPanel = new JPanel();
        combinationPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        combinationPanel.add(new JLabel("Number of Cypher: "));
        combinationPanel.add(combinationComboBox);
        generalPanel.add(combinationPanel);

        generalPanel.add(logLabel);

        startStopButton.addActionListener((e) -> {
            if (startStopButton.getText().equals(START)) {
                playerComboBox.setEnabled(false);
                combinationComboBox.setEnabled(false);
                startStopButton.setText(STOP);

                // start Game.
                this.guiActor.sendStartGame(this.getPlayersNumber(), this.getCombinationSize());
                // todo create new JFrame for player
            } else {
                this.finishButton();
            }
        });
        generalPanel.add(startStopButton);

        mainFrame.setContentPane(generalPanel);
        mainFrame.pack();
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent ev) {
                System.exit(-1);
            }
            public void windowClosed(final WindowEvent ev) {
                System.exit(-1);
            }
        });
        mainFrame.setVisible(true);
    }


    /**
     * Init the game.
     * It's called when the game is finished.
     */
    private void finishButton() {
        playerComboBox.setEnabled(true);
        combinationComboBox.setEnabled(true);
        startStopButton.setText(START);
    }


    @Override
    public void update(final String logMsg) {
        this.logLabel.setText(logMsg);
    }


    @Override
    public void finish(final Message msg) {
        this.finishButton();
        if (msg instanceof WinMsg) {
            this.update("Player " + ((WinMsg) msg).getWinnerRef() + " have won the game!");
        } else {
            this.update("All players have lost!");
        }
    }

    private int getPlayersNumber() {
        return (playerComboBox.getSelectedItem() != null) ? (Integer) playerComboBox.getSelectedItem() : 1;
    }


    private int getCombinationSize() {
        return (combinationComboBox.getSelectedItem() != null) ? (Integer) combinationComboBox.getSelectedItem() : 1;
    }
}
