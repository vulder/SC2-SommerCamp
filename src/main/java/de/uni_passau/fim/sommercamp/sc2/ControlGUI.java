package de.uni_passau.fim.sommercamp.sc2;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;

public class ControlGUI {
    private JPanel basePanel;
    private JButton singlePlayerButton;
    private JButton multiPlayerButton;
    private JComboBox<String> mapSelector;
    private JPanel botsPanel;
    private JScrollPane botScroll;
    private JButton reloadButton;
    private JSlider fps;

    private List<JToggleButton> botSelector = new ArrayList<>();

    private ControlGUI(List<String> bots, List<String> maps) {

        JFrame frame = new JFrame("SC2-Bots Starter");
        frame.setContentPane(basePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        for (int i = 0; i < bots.size(); i++) {
            String bot = bots.get(i);
            JToggleButton button = new JToggleButton(bot);
            botSelector.add(button);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.fill = 1;
            botsPanel.add(button, gbc);
        }

        frame.pack();
        int botsWidth = botsPanel.getWidth();
        botsPanel.setPreferredSize(new Dimension(botsWidth, 0));
        botScroll.setViewportView(botsPanel);
        botScroll.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        botScroll.setPreferredSize(new Dimension(botsWidth, 0));

        maps.forEach(mapSelector::addItem);

        singlePlayerButton.addActionListener(e -> new Thread(() ->
                botSelector.stream().filter(JToggleButton::isSelected).map(JToggleButton::getText).findFirst().ifPresent(b -> {
                    try {
                        BaseBot.FRAME_RATE = fps.getValue();
                        SinglePlayerMain.run((String) mapSelector.getSelectedItem(), b);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
        )).start());
        multiPlayerButton.addActionListener(e -> new Thread(() -> {
            try {
                MultiPlayerMain.run((String) mapSelector.getSelectedItem(), botSelector.stream().filter(JToggleButton::isSelected).map(JToggleButton::getText).collect(Collectors.toList()));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }).start());

        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        new ControlGUI(ReflectionUtil.getBotList(), ReflectionUtil.getMaps());

    }

    private void createUIComponents() {
        botsPanel = new JPanel(new GridBagLayout());
    }
}
