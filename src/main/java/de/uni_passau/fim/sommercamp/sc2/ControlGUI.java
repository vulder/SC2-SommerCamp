package de.uni_passau.fim.sommercamp.sc2;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ControlGUI {
    private JPanel basePanel;
    private JButton singlePlayerButton;
    private JButton multiPlayerButton;
    private JComboBox<String> mapSelector;
    private JPanel botsPanel;
    private JScrollPane botScroll;

    private List<JToggleButton> botSelector = new ArrayList<>();

    private ControlGUI(List<String> bots, List<String> maps) {

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

        botScroll.setViewportView(botsPanel);

        maps.forEach(mapSelector::addItem);

        singlePlayerButton.addActionListener(e -> new Thread(() ->
                botSelector.stream().filter(JToggleButton::isSelected).map(JToggleButton::getText).findFirst().ifPresent(b -> {
                    try {
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

        JFrame frame = new JFrame("SC2-Bots Starter");
        frame.setContentPane(basePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
