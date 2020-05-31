package pcd.ass03.ex01.graphic;

import pcd.ass03.ex01.actors.GuiActor;
import pcd.ass03.ex01.messages.Message;
import pcd.ass03.ex01.messages.WinMsg;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class GameGuiImpl implements GameGui {

    private static final int WIDTH = 500;
    private static final int HEIGHT = 30;
    private static final String START_LABEL = "Start";
    private static final String STOP_LABEL = "Stop";
    private static final Integer[] COMB_SIZE_ITEMS = new Integer[] {1, 2, 3, 4, 5};
    private static final Integer[] N_PLAYERS_ITEMS = new Integer[] {2, 3, 4, 5, 6, 7, 8};
    private static final int DEFAULT_N_PLAYERS = 2;
    private static final int DEFAULT_COMB_SIZE = 4;

    /**
     * Reference to GuiActor. Used to interact with the rest of the game.
     */
    private final GuiActor guiActor;

    /**
     * The main frame of the gui.
     */
    private final JFrame mainFrame;

    /**
     * Button used to start and force stop the game.
     */
    private final JButton startStopButton;


    /**
     * ComboBox used to select the size of the combinations used by each player
     * of the game.
     */
    private final JComboBox<Integer> combSizeComboBox;


    /**
     * ComboBox used to select the number of players participating to the game.
     */
    private final JComboBox<Integer> nPlayersComboBox;


    /**
     * CheckBox used to select whether to participate directly to the game as
     * a player.
     */
    private final JCheckBox humanPlayerCheckBox;


    /**
     * CheckBox used to select an optinal project constraint: set to true if
     * a guess' response have to be sent to all players,or only to the
     * player that sent the guess.
     */
    private final JCheckBox respOnlyToGuesserCheckBox;


    /**
     * Label used to show the last retrieved log. The complete log is
     * displayed directly into the shell.
     */
    private final JLabel logLabel;

    /**
     * Keeps track of whether the game has started.
     */
    private boolean isGameStarted;


    /**
     * Default constructor of the class. It initializes the frame, without
     * launching it.
     * @param actorGui reference to the gui actor.
     */
    GameGuiImpl(final GuiActor actorGui) {
        this.guiActor = actorGui;

        this.isGameStarted = false;
        this.combSizeComboBox = new JComboBox<>(COMB_SIZE_ITEMS);
        this.combSizeComboBox.setSelectedIndex(DEFAULT_COMB_SIZE);
        this.nPlayersComboBox = new JComboBox<>(N_PLAYERS_ITEMS);
        this.nPlayersComboBox.setSelectedItem(DEFAULT_N_PLAYERS);

        this.startStopButton = new JButton(START_LABEL);
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(startStopButton);

        this.humanPlayerCheckBox = new JCheckBox();
        this.respOnlyToGuesserCheckBox = new JCheckBox();

        this.logLabel = new JLabel("Choose your settings and start the game!");
        this.logLabel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        final JPanel logPanel = new JPanel();
        logPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        logPanel.add(logLabel);

        this.mainFrame = new JFrame();
        this.mainFrame.setTitle("PCD - Assignment 03 - Maldini, Gorini, Angelini - MasterMind");
        this.mainFrame.setResizable(false);

        final JPanel generalPanel = new JPanel();
        generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.PAGE_AXIS));

        generalPanel.add(generateSinglePanel("Number of players: ", nPlayersComboBox));
        generalPanel.add(generateSinglePanel("Combination size: ", combSizeComboBox));
        generalPanel.add(generateSinglePanel("Do you want a human player? ", humanPlayerCheckBox));
        generalPanel.add(generateSinglePanel("Do you want to send guess' response only to the sender player? ", respOnlyToGuesserCheckBox));

        generalPanel.add(logPanel);
        generalPanel.add(buttonPanel);

        startStopButton.addActionListener((e) -> {
            SwingUtilities.invokeLater(() -> {
                if (isGameStarted) {
                    isGameStarted = false;
                    enableGuiComponents();
                    guiActor.sendStopGameMessage();

                } else {
                    isGameStarted = true;
                    disableGuiComponents();
                    guiActor.sendStartGameMessage();
                }
            });
        });

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

    }


    /**
     * Initializes a single panel of the gui, composed by a label to describe
     * it and an interactive component.
     * @param description is the label's name.
     * @param interactiveComp is the component to add at the panel.
     * @return the fresh new panel.
     */
    private JPanel generateSinglePanel(final String description, final JComponent interactiveComp) {
        final JPanel newPanel = new JPanel();
        newPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        newPanel.add(new JLabel(description));
        newPanel.add(interactiveComp);
        return newPanel;
    }


    @Override
    public void launch() {
        mainFrame.setVisible(true);
    }


    @Override
    public void printLog(final String logMsg) {
        this.logLabel.setText(logMsg);
    }


    @Override
    public void finishMatch(final Message msg) {
        this.isGameStarted = false;
        this.enableGuiComponents();
        if (msg instanceof WinMsg) {
            this.printLog("Player " + ((WinMsg) msg).getWinnerRef().path().name() + " have won the game!");
        } else {
            this.printLog("All players have lost!");
        }
    }


    @Override
    public int getNPlayers() {
        return (nPlayersComboBox.getSelectedItem() != null)
                ? (Integer) nPlayersComboBox.getSelectedItem()
                : DEFAULT_N_PLAYERS;
    }


    @Override
    public int getCombSize() {
        return (combSizeComboBox.getSelectedItem() != null)
                ? (Integer) combSizeComboBox.getSelectedItem()
                : DEFAULT_COMB_SIZE;
    }


    @Override
    public boolean hasHumanPlayer() {
        return humanPlayerCheckBox.isSelected();
    }


    @Override
    public boolean respondsOnlyToTheGuesser() {
        return respOnlyToGuesserCheckBox.isSelected();
    }


    /**
     * Disables interactive components of the gui, used for give the game
     * configuration. It's called when the game is started.
     */
    private void disableGuiComponents() {
        nPlayersComboBox.setEnabled(false);
        combSizeComboBox.setEnabled(false);
        humanPlayerCheckBox.setEnabled(false);
        respOnlyToGuesserCheckBox.setEnabled(false);
        startStopButton.setText(STOP_LABEL);
    }


    /**
     * Enables interactive components of the gui, after the end of the
     * game.
     */
    private void enableGuiComponents() {
        nPlayersComboBox.setEnabled(true);
        combSizeComboBox.setEnabled(true);
        humanPlayerCheckBox.setEnabled(true);
        respOnlyToGuesserCheckBox.setEnabled(true);
        startStopButton.setText(START_LABEL);
    }
}
