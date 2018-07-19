package de.uni_passau.fim.sommercamp.sc2;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;

public class ControlGUI {
    private JPanel basePanel;
    private JButton singlePlayerButton;
    private JButton multiPlayerButton;
    private JComboBox<String> mapSelector;
    private JPanel botAPanel;
    private JScrollPane botAScroll;
    private JSlider fps;
    private JPanel botBPanel;
    private JScrollPane botBScroll;

    private List<JToggleButton> botASelector = new ArrayList<>();
    private List<JToggleButton> botBSelector = new ArrayList<>();

    private ControlGUI(List<String> bots, List<String> maps) {

        JFrame frame = new JFrame("SC2-Bots Starter");
        frame.setContentPane(basePanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        for (int i = 0; i < bots.size(); i++) {
            String bot = bots.get(i);
            JToggleButton button = new JToggleButton(bot);
            botASelector.add(button);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.fill = 1;
            botAPanel.add(button, gbc);
        }

        for (int i = 0; i < bots.size(); i++) {
            String bot = bots.get(i);
            JToggleButton button = new JToggleButton(bot);
            botBSelector.add(button);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.fill = 1;
            botBPanel.add(button, gbc);
        }

        frame.pack();
        int botsWidth = botAPanel.getWidth();
        int botsHeight = botAPanel.getHeight();
        botAPanel.setPreferredSize(new Dimension(botsWidth, botsHeight));
        botBPanel.setPreferredSize(new Dimension(botsWidth, botsHeight));
        botAScroll.setViewportView(botAPanel);
        botBScroll.setViewportView(botBPanel);
        botAScroll.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        botBScroll.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        botAScroll.setPreferredSize(new Dimension(botsWidth, 0));
        botBScroll.setPreferredSize(new Dimension(botsWidth, 0));

        maps.forEach(mapSelector::addItem);

        singlePlayerButton.addActionListener(e -> new Thread(() ->
                botASelector.stream().filter(JToggleButton::isSelected).map(JToggleButton::getText).findFirst().ifPresent(b -> {
                            BaseBot.FRAME_RATE = fps.getValue();
                            SinglePlayerMain.run((String) mapSelector.getSelectedItem(), b);
                })).start());

        multiPlayerButton.addActionListener(e -> new Thread(() ->
                botASelector.stream().filter(JToggleButton::isSelected).map(JToggleButton::getText).findFirst().ifPresent(botA ->
                        botBSelector.stream().filter(JToggleButton::isSelected).map(JToggleButton::getText).findFirst().ifPresent(botB -> {
                            BaseBot.FRAME_RATE = fps.getValue();
                            MultiPlayerMain.run((String) mapSelector.getSelectedItem(), Arrays.asList(botA, botB));
                        }))).start());

        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new ControlGUI(ReflectionUtil.getBotList(), ReflectionUtil.getMaps());
    }

    private void createUIComponents() {
        botAPanel = new JPanel(new GridBagLayout());
        botBPanel = new JPanel(new GridBagLayout());
    }
}
