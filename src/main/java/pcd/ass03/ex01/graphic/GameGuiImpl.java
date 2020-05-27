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
     * CheckBox that notify if a player human is selected.
     */
    private final JCheckBox checkHumanPlayer = new JCheckBox();


    /**
     * CheckBox that check if a guess'response have to be sended at all players
     * or only the player that send the guess.
     */
    private final JCheckBox responseOnlyAPlayer = new JCheckBox();

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

        final Integer[] cypherNumber = new Integer[] {1, 2, 3, 4, 5};
        combinationComboBox = new JComboBox<>(cypherNumber);

        final Integer[] playerNumber = new Integer[] {2, 3, 4, 5, 6, 7, 8 };
        this.playerComboBox = new JComboBox<>(playerNumber);

        this.startStopButton = new JButton(START);

        this.logLabel = new JLabel("Chose your settings and start the game!");
        this.logLabel.setPreferredSize(new Dimension(GameGui.WEIGHT, GameGui.HEIGHT));

        this.launch();
    }


    private void launch() {
        JFrame mainFrame = new JFrame();

        mainFrame.setTitle("PCD - Assignment 03 - Maldini, Gorini, Angelini - MasterMind");
        mainFrame.setResizable(false);

        JPanel generalPanel = new JPanel();
        generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.PAGE_AXIS));

        initPanel("Number of Player: ", playerComboBox, generalPanel);
        initPanel("Number of Cypher: ", combinationComboBox, generalPanel);
        initPanel("Do you want a human player? ", checkHumanPlayer, generalPanel);
        initPanel("Do you want to send guess' response only to the sender player? ", responseOnlyAPlayer, generalPanel);

        generalPanel.add(logLabel);

        startStopButton.addActionListener((e) -> {
            if (startStopButton.getText().equals(START)) {
                this.callStart();

                this.guiActor.sendStartGame(this.getPlayersNumber(),
                                            this.getCombinationSize(),
                                            this.getCheckHumanPlayer(),
                                            this.getResponseOnlyAPlayer());
                // todo create new JFrame for human player
            } else {
                this.callFinish();
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
     * Blocked the game.
     * It's called when the game is started.
     */
    private void callStart() {
        playerComboBox.setEnabled(false);
        combinationComboBox.setEnabled(false);
        checkHumanPlayer.setEnabled(false);
        startStopButton.setText(STOP);
    }


    /**
     * Init the game.
     * It's called when the game is finished.
     */
    private void callFinish() {
        playerComboBox.setEnabled(true);
        combinationComboBox.setEnabled(true);
        checkHumanPlayer.setEnabled(true);
        startStopButton.setText(START);
    }


    @Override
    public void update(final String logMsg) {
        this.logLabel.setText(logMsg);
    }


    @Override
    public void finish(final Message msg) {
        this.callFinish();
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

    private boolean getCheckHumanPlayer() {
        return checkHumanPlayer.isSelected();
    }

    private boolean getResponseOnlyAPlayer() {
        return responseOnlyAPlayer.isSelected();
    }

    /**
     * Initialize every panel in the Gui.
     * @param labelName is the label's name.
     * @param panelComponent is the component to add at the panel.
     * @param panelToAdd is the general panel to add.
     */
    private void initPanel(final String labelName, final JComponent panelComponent, final JPanel panelToAdd) {
        final JPanel newPanel = new JPanel();
        newPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        newPanel.add(new JLabel(labelName));
        newPanel.add(panelComponent);
        panelToAdd.add(newPanel);
    }
}
