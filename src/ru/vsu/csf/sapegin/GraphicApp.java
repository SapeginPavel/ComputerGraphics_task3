package ru.vsu.csf.sapegin;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class GraphicApp {
    private JFrame frame;
    private GraphicPanel graphicPanel;

    private final JLabel labelFunc = new JLabel("Введите функцию: ");
    private final JTextField fieldFunction = new JTextField(""); //ax^4 + bcx^3 + hx - 0.2
    private final JButton buttBuildGraphic = new JButton("построить");

    private final JLabel labelMove = new JLabel("Перемещение: ");
    private final JButton buttMoveLeft = new JButton("←");
    private final JButton buttMoveUp = new JButton("↑");
    private final JButton buttMoveRight = new JButton("→");
    private final JButton buttMoveDown = new JButton("↓");
    private final JTextField fieldMove = new JTextField("20");

    private final JLabel labelScale = new JLabel("Масштаб: ");
    private final JButton buttIncreaseScale = new JButton("+");
    private final JButton buttDecreaseScale = new JButton("-");

    private final JButton buttStartScale = new JButton("вернуть масштаб");

    private final Box bottomPanelBox = Box.createHorizontalBox();

    private final JSpinner numOfPointsSpinner = new JSpinner(new SpinnerNumberModel(21, 3, 100_000, 1));

    public GraphicApp() {
        createFrame();
        initElements();

        buttBuildGraphic.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String functionAsString = getFunctionAsString().replaceAll("\\s+","");
                char[] functionAsChars = functionAsString.toCharArray();
                List<Character> functionAsCharsWithParameters = new ArrayList<>();
                List<Character> arithmeticSigns = new ArrayList<>();
                arithmeticSigns.add('+');
                arithmeticSigns.add('-');
                arithmeticSigns.add('/');
                arithmeticSigns.add('*');
                arithmeticSigns.add('^');
                //не будет работать корень из параметра, и вообще корень не будет норм работать...
                //чтобы он работал, нужно переписать его задание
                for (char tempFuncChar : functionAsChars) {
                    if (tempFuncChar >= 97 && tempFuncChar <= 119) {
                        String potentialNameOfSlider = Character.toString(tempFuncChar);
                        for (Component component : bottomPanelBox.getComponents()) {
                            if (component.getName().equals(potentialNameOfSlider) && component instanceof JSlider) {
                                double param = (((JSlider) component).getValue()) / 7.0;
                                if (!functionAsCharsWithParameters.isEmpty()) {
                                    char previous = functionAsCharsWithParameters.get(functionAsCharsWithParameters.size() - 1);
                                    if (!arithmeticSigns.contains(previous)) {
                                        functionAsCharsWithParameters.add('*');
                                    }
                                }
                                for (char forAddingChar : Double.toString(param).toCharArray()) {
                                    functionAsCharsWithParameters.add(forAddingChar);
                                }
                            }
                        }
                    } else {
                        functionAsCharsWithParameters.add(tempFuncChar);
                    }
                }
                StringBuilder stringBuilder = new StringBuilder();
                for (char c : functionAsCharsWithParameters) {
                    stringBuilder.append(c);
                }
                graphicPanel.setNumOfPoints(getNumOfPoints());
                graphicPanel.setFunction(stringBuilder.toString());
            }
        });
        buttMoveLeft.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphicPanel.setNewMove(-getFieldMove(),0);
            }
        });
        buttMoveUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphicPanel.setNewMove(0, getFieldMove());
            }
        });
        buttMoveDown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphicPanel.setNewMove(0,-getFieldMove());
            }
        });
        buttMoveRight.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphicPanel.setNewMove(getFieldMove(),0);
            }
        });
        buttDecreaseScale.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphicPanel.setNewScale(0.9);
            }
        });
        buttIncreaseScale.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphicPanel.setNewScale(1.1);
            }
        });
        buttStartScale.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphicPanel.setStartScale();
            }
        });
        fieldFunction.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                List<Character> parameters = getParametersFromFunctionAsString(getFunctionAsString().substring(fieldFunction.getCaretPosition()));
                for (Component component : bottomPanelBox.getComponents()) {
                    String componentName = component.getName();
                    for (Character parameter : parameters) {
                        if (Character.toString(parameter).equals(componentName)) {
                            return;
                        }
                    }
                }
                for (char c : parameters) {
                    String param = Character.toString(c);
                    JLabel label = new JLabel(param + " * 7");
                    label.setName(param);
                    bottomPanelBox.add(label);
                    JSlider slider = new JSlider(SwingConstants.HORIZONTAL,0, 21, 7);
                    slider.setName(param);
                    slider.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            buttBuildGraphic.doClick();
                        }
                    });
                    slider.setMajorTickSpacing(7);
                    slider.setPaintLabels(true);
                    bottomPanelBox.add(slider);
                    frame.revalidate();
                    frame.repaint();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                List<Character> parameters = getParametersFromFunctionAsString(getFunctionAsString());
                if (parameters.isEmpty()) {
                    bottomPanelBox.removeAll();
                }
                for (Component component : bottomPanelBox.getComponents()) {
                    String componentName = component.getName();
                    for (int i = 0; i < parameters.size(); i++) {
                        if (Character.toString(parameters.get(i)).equals(componentName)) {
                            break;
                        } else if (i == parameters.size() - 1) {
                            bottomPanelBox.remove(component);
                        }
                    }
                }
                frame.revalidate();
                frame.repaint();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    private List<Character> getParametersFromFunctionAsString(String functionAsString) {
        char[] chars = functionAsString.toCharArray();
        List<Character> parameters = new ArrayList<>();
        for (char c : chars) {
            if (c >= 97 && c <= 119) {
                parameters.add(c);
            }
        }
        return parameters;
    }

    private void createFrame() {
        frame = new JFrame("Построение функции");
        frame.setSize(1200, 600);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public void show() {
        frame.setVisible(true);
    }

    private void initElements() {
        Container container = frame.getContentPane();
        container.setLayout(new BorderLayout());

        Box topPanelBox = Box.createHorizontalBox();
        fieldFunction.setMaximumSize(new Dimension(200, 25));
        fieldMove.setMaximumSize(new Dimension(50, 25));
        topPanelBox.setBorder(BorderFactory.createEmptyBorder(20, 20,  20, 20));
        topPanelBox.add(labelFunc);
        topPanelBox.add(fieldFunction);
        topPanelBox.add(buttBuildGraphic);
        topPanelBox.add(new JLabel("   "));
        topPanelBox.add(labelMove);
        topPanelBox.add(new JLabel("   "));
        topPanelBox.add(fieldMove);
        topPanelBox.add(new JLabel("   "));
        topPanelBox.add(buttMoveLeft);
        topPanelBox.add(buttMoveUp);
        topPanelBox.add(buttMoveDown);
        topPanelBox.add(buttMoveRight);

        topPanelBox.add(new JLabel("   "));
        topPanelBox.add(labelScale);
        topPanelBox.add(buttDecreaseScale);
        topPanelBox.add(buttIncreaseScale);
        container.add(topPanelBox, BorderLayout.NORTH);
        topPanelBox.add(new JLabel("   "));
        numOfPointsSpinner.setMinimumSize(new Dimension(30, 20));
        numOfPointsSpinner.setMaximumSize(new Dimension(40, 20));
        topPanelBox.add(new JLabel("Кол-во точек: "));
        topPanelBox.add(numOfPointsSpinner);
        topPanelBox.add(new JLabel("   "));
        topPanelBox.add(buttStartScale);

        graphicPanel = new GraphicPanel();
        frame.add(graphicPanel, BorderLayout.CENTER);

        bottomPanelBox.setBorder((BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        frame.add(bottomPanelBox, BorderLayout.SOUTH);
    }

    private int getFieldMove() {
        return Integer.parseInt(fieldMove.getText());
    }

    private String getFunctionAsString() {
        return fieldFunction.getText();
    }

    private int getNumOfPoints() {
        return (Integer) numOfPointsSpinner.getValue();
    }
}
