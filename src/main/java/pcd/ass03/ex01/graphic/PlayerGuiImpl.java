package pcd.ass03.ex01.graphic;

import akka.actor.ActorRef;
import pcd.ass03.ex01.actors.HumanPlayerActor;
import pcd.ass03.ex01.utils.Combination;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PlayerGuiImpl implements PlayerGui {
    private static final String DEFAULT_STATE_LABEL = "Welcome, player!";

    private final HumanPlayerActor player;
    private final List<ActorRef> enemies;
    private final int combSize;

    private final JTextField guessTextField;
    private final JComboBox<Object> enemiesComboBox;
    private final JButton sendGuessButton;

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
        this.guessTextField.setColumns(5);
        this.enemiesComboBox = new JComboBox<>(extractEnemiesName());
        this.sendGuessButton = new JButton("Send guess");

        this.finishTurnButton = new JButton("Finish turn without solution");

        this.solutionTextField = new JTextField(generateSampleSolutionComb());
        this.solutionTextField.setColumns(15);
        this.sendSolutionButton = new JButton("Send this solution");

        this.stateLabel = new JLabel(DEFAULT_STATE_LABEL);

        this.mainFrame = new JFrame();
        this.mainFrame.setTitle("Collective MasterMind - Player view");
        this.mainFrame.setResizable(false);

        final JPanel generalPanel = new JPanel();
        generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.PAGE_AXIS));

        generalPanel.add(generateLinePanel(new JLabel("1. Make a guess")));
        generalPanel.add(generateLinePanel(enemiesComboBox, guessTextField, sendGuessButton));

        generalPanel.add(generateLinePanel(new JLabel("2. Finish the turn")));
        generalPanel.add(generateLinePanel(solutionTextField, sendSolutionButton, finishTurnButton));
        generalPanel.add(generateLinePanel(stateLabel));

        sendGuessButton.addActionListener((e) -> {
            SwingUtilities.invokeLater(() -> {
               final String guessString = guessTextField.getText();
               if (guessString.matches(generateGuessCombRegex())) {
                   final Combination guess = Combination.of(parseCombElements(guessString));
                   player.sendGuessMsg(getSelectedEnemy(), guess);
                   enableSolutionComponents();

               } else {
                   stateLabel.setText("Wrong guess format. Try again.");
               }
            });
        });

        finishTurnButton.addActionListener((e) -> {
            SwingUtilities.invokeLater(() -> {
                player.sendFinishTurnMsg();
                disableAllComponents();
            });
        });

        sendSolutionButton.addActionListener((e) -> {
            SwingUtilities.invokeLater(() -> {
                final String solutionString = solutionTextField.getText();
                if (solutionString.matches(generateSolutionCombRegex())) {
                    player.sendSolutionMsg(parseSolution(solutionString));
                    enableSolutionComponents();

                } else {
                    stateLabel.setText("Wrong solution format. Try again.");
                }
                disableAllComponents();
            });
        });

        mainFrame.setContentPane(generalPanel);
        mainFrame.pack();
    }


    @Override
    public void launch() {
        mainFrame.setVisible(true);
        disableAllComponents();
    }


    @Override
    public void matchLoss(final boolean wrongSolution) {
        disableAllComponents();
        if (wrongSolution) {
            stateLabel.setText("Wrong solution! You have lost match!");

        } else {
            final String lostString = "No one won the match!";
            windowExitTimeout(lostString);
        }
    }


    @Override
    public void matchVictory(final ActorRef winner) {
        disableAllComponents();
        final String winLostString;
        if (winner.equals(player.getSelf())) {
            winLostString = "You win the match!";
        } else {
            winLostString = winner.path().name() + " won the match!";
        }

        windowExitTimeout(winLostString);
    }


    private void windowExitTimeout(final String stateLabelString) {
        stateLabel.setText(stateLabelString + " The window will be closed in 3 seconds...");

        final Thread exitTimeout = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(1000);
                    stateLabel.setText(stateLabelString + " The window will be closed in 2 seconds...");
                    Thread.sleep(1000);
                    stateLabel.setText(stateLabelString + " The window will be closed in 1 seconds... bye!");
                    Thread.sleep(1000);
                    mainFrame.setVisible(false);
                    mainFrame.dispose();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        exitTimeout.start();
    }


    @Override
    public void timeoutReceived() {
        disableAllComponents();
        stateLabel.setText("Too much time elapsed! Turn finished.");
    }

    @Override
    public void startTurn() {
        stateLabel.setText("It's your turn! select a guess in the format " + generateSampleGuessComb());
        enableGuessComponents();
    }


    @Override
    public void startFinalTurnPart(final int guessedPos, final int guessedCyphers) {
        stateLabel.setText("Player responded. Guessed cyphers: " + guessedCyphers + "; guessed positions: " + guessedPos + ".");
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


    private Object[] extractEnemiesName() {
        return enemies.stream().map(enemy -> enemy.path().name()).toArray();
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


    private Map<ActorRef, Combination> parseSolution(final String solutionString) {
        final Map<ActorRef, Combination> solution = new HashMap<>();
        final String[] partSolutionsString = solutionString.split(",");

        for (int i = 0; i < enemies.size(); i++) {
            solution.put(enemies.get(i), Combination.of(parseCombElements(partSolutionsString[i])));
        }

        return solution;
    }
}
