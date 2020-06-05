package pcd.ass03.ex01.graphic;

import akka.actor.ActorRef;
import pcd.ass03.ex01.actors.HumanPlayerActor;
import pcd.ass03.ex01.actors.PlayerActor;
import pcd.ass03.ex01.utils.Combination;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public final class PlayerGuiImpl implements PlayerGui {
    private static final String DEFAULT_GUESS_RESPONSE_TEXT = "Here will be displayed the guess response.";
    private static final String DEFAULT_STATE_LABEL = "Welcome, player!";

    private final HumanPlayerActor player;
    private final List<ActorRef> enemies;
    private final int combSize;

    private final JTextField guessTextField;
    private final JComboBox<String> enemiesComboBox;
    private final JButton sendGuessButton;

    private final JLabel guessResponseLabel;
    private final JButton finishTurnButton;

    private final JTextField solutionTextField;
    private final JButton sendSolutionButton;

    private final JLabel stateLabel;

    /**
     * The main frame of the gui.
     */
    private final JFrame mainFrame;


    /**
     * Default constructor. It configures the gui, without launching it.
     * @param playerActor a reference to the correspondent actor.
     */
    PlayerGuiImpl(final HumanPlayerActor playerActor, final List<ActorRef> enemies, final int combSize) {
        this.player = playerActor;
        this.enemies = enemies;
        this.combSize = combSize;

        this.guessTextField = new JTextField(generateSampleGuessComb());
        this.enemiesComboBox = new JComboBox<>(extractEnemiesName());
        this.sendGuessButton = new JButton("Send guess");

        this.guessResponseLabel = new JLabel(DEFAULT_GUESS_RESPONSE_TEXT);
        this.finishTurnButton = new JButton("Finish turn");

        this.solutionTextField = new JTextField(generateSampleSolutionComb());
        this.sendSolutionButton = new JButton("Send solution");

        this.stateLabel = new JLabel(DEFAULT_STATE_LABEL);

        this.mainFrame = new JFrame();
        this.mainFrame.setTitle("MasterMind - Player view");
        this.mainFrame.setResizable(false);

        final JPanel generalPanel = new JPanel();
        generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.PAGE_AXIS));

        JLabel guessDescription = new JLabel("Choose a guess in the format " + generateSampleGuessComb());
        generalPanel.add(generateLinePanel(guessDescription, guessTextField, enemiesComboBox));
        generalPanel.add(generateLinePanel(guessResponseLabel, finishTurnButton));
        generalPanel.add(generateLinePanel(solutionTextField, sendSolutionButton));
        generalPanel.add(generateLinePanel(stateLabel));

        sendGuessButton.addActionListener((e) -> {
            SwingUtilities.invokeLater(() -> {
               final String guessString = guessTextField.getText();
               if (guessString.matches(generateGuessCombRegex())) {
                   final Combination guess = Combination.of(parseCombElements(guessString));
                   player.sendGuessMessage(getSelectedEnemy(), guess);
                   enableSolutionComponents();

               } else {
                   stateLabel.setText("Wrong guess format. Try again.");
               }
            });
        });

        finishTurnButton.addActionListener((e) -> {
            SwingUtilities.invokeLater(() -> {
                // todo finish turn msg
                disableAllComponents();
            });
        });

        sendSolutionButton.addActionListener((e) -> {
            SwingUtilities.invokeLater(() -> {
                // todo parse solution and send it
                disableAllComponents();
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


    @Override
    public void launch() {
        mainFrame.setVisible(true);
        disableAllComponents();
    }


    @Override
    public void loseMatch() {
        disableAllComponents();
        stateLabel.setText("You lost the match!");
    }


    @Override
    public void finishMatch(final String winnerName, final boolean youWin) {
        disableAllComponents();
        if (youWin) {
            stateLabel.setText("You win the match!");
        } else{
            stateLabel.setText(winnerName + " wins the match!");
        }
    }


    @Override
    public void startTurn() {
        stateLabel.setText("It's your turn! select a guess in the format " + generateSampleGuessComb());
        enableGuessComponents();
    }


    @Override
    public void startFinalTurnPart(final Combination guessResponse) {
        stateLabel.setText("Player responded with " + guessResponse.toString() + "; finish your turn, or select a solution.");
        enableSolutionComponents();
    }

    /**
     * Generates a JPanel that has n elements in line.
     * @param components the components from left to right.
     * @return the formatted panel.
     */
    private JPanel generateLinePanel(final JComponent... components) {
        final JPanel newPanel = new JPanel();
        newPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        for (JComponent component : components) {
            newPanel.add(component);
        }

        return newPanel;
    }


    private String[] extractEnemiesName() {
        return (String[]) enemies.stream().map(enemy -> enemy.path().name()).toArray();
    }


    private String generateSampleGuessComb() {
        final StringBuilder sampleComb = new StringBuilder();
        for (int i = 0; i < combSize; i++) {
            sampleComb.append("0");
        }
        return sampleComb.toString();
    }

    private String generateSampleSolutionComb() {
        final StringBuilder sampleComb = new StringBuilder();
        for (int i = 0; i < enemies.size(); i++) {
            sampleComb.append(generateSampleGuessComb());
            if (i != enemies.size() - 1) {
                sampleComb.append(",");
            }
        }
        return sampleComb.toString();
    }


    private String generateGuessCombRegex() {
        final StringBuilder sampleComb = new StringBuilder();
        for (int i = 0; i < combSize; i++) {
            sampleComb.append("[0-9]");
        }
        return sampleComb.toString();
    }

    private String generateSolutionCombRegex() {
        final StringBuilder sampleComb = new StringBuilder();
        for (int i = 0; i < enemies.size(); i++) {
            sampleComb.append(generateGuessCombRegex());
            if (i != enemies.size() - 1) {
                sampleComb.append(",");
            }
        }
        return sampleComb.toString();
    }

    private void enableGuessComponents() {
        guessTextField.setEnabled(true);
        enemiesComboBox.setEnabled(true);
        sendGuessButton.setEnabled(true);

        finishTurnButton.setEnabled(false);
        solutionTextField.setEnabled(false);
        sendSolutionButton.setEnabled(false);
    }

    private void enableSolutionComponents() {
        guessTextField.setEnabled(false);
        enemiesComboBox.setEnabled(false);
        sendGuessButton.setEnabled(false);

        finishTurnButton.setEnabled(true);
        solutionTextField.setEnabled(true);
        sendSolutionButton.setEnabled(true);
    }

    private void disableAllComponents() {
        guessTextField.setEnabled(false);
        enemiesComboBox.setEnabled(false);
        sendGuessButton.setEnabled(false);

        finishTurnButton.setEnabled(false);
        solutionTextField.setEnabled(false);
        sendSolutionButton.setEnabled(false);
    }


    private ActorRef getSelectedEnemy() {
        return enemies.get(enemiesComboBox.getSelectedIndex());
    }


    private List<Integer> parseCombElements(String combString) {
        final List<Integer> combElements = new ArrayList<>();
        for (int i = 0; i < combString.length(); i++) {
            combElements.add(Integer.parseInt(combString.substring(i, i+1)));
        }
        return combElements;
    }
}
