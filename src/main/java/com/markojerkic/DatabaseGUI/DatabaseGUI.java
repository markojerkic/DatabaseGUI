package com.markojerkic.DatabaseGUI;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseGUI {


    private FileInputStream inputStream;
    private GoogleCredentials googleCredentials;
    private FirebaseOptions firebaseOptions;
    private Firestore firestore;

    private JFrame frame;
    private JLabel label;
    private GridBagConstraints constraints;

    // Elements

    // Top panel
    private JLabel questionLabel;
    private JTextField questionEntry;
    private JLabel answerALabel;
    private JTextField answerAEntry;
    private JLabel answerBLabel;
    private JTextField answerBEntry;
    private JLabel answerCLabel;
    private JTextField answerCEntry;
    private JLabel answerDLabel;
    private JTextField answerDEntry;
    // Center panel
    private JButton addPicatureButton;
    private JLabel picatureLabel;
    // Bottom panel
    private JLabel subjectLabel;
    private JTextField subjectEntry;
    private JComboBox<String> subjectComboBox;
    private JLabel yearLabel;
    private JTextField yearEntry;
    private JComboBox<String> yearComboBox;

    // Type of answer
    private ButtonGroup typeOfAnswerGroup;
    private JRadioButton ansABCD;
    private JRadioButton ansType;
    private JRadioButton ansLong;

    private ButtonGroup buttonGroup;
    private JRadioButton ansARadio;
    private JRadioButton ansBRadio;
    private JRadioButton ansCRadio;
    private JRadioButton ansDRadio;

    private JMenuBar menuBar;
    private JMenu mainMenu;
    private JMenuItem exitMenuItem;
    private JMenuItem submitMenuItem;

    private int answerType = 0;
    private boolean imageAdded = false;
    private String imageURI;

    private String[] SUBJECTS = new String[] {"Matematika - Viša Razina", "Matematika - Osnovna Razina"};
    private String[] YEARS = new String[] {"2019./20.", "2018./19.", "2017./28."};

    public DatabaseGUI() {

        try {
            inputStream = new FileInputStream("C:\\Users\\marko\\Documents\\admin_sdk.json");
            googleCredentials = GoogleCredentials.fromStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        firebaseOptions = new FirebaseOptions.Builder().setCredentials(googleCredentials)
                .setDatabaseUrl("https://drzavna-matura-1fbe7.firebaseio.com").build();
        FirebaseApp.initializeApp(firebaseOptions);
        firestore = FirestoreClient.getFirestore();


        frame = new JFrame("Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panelTop = new JPanel();

        menuBar = new JMenuBar();
        mainMenu = new JMenu("Menu");
        mainMenu.setMnemonic(KeyEvent.VK_M);
        menuBar.add(mainMenu);
        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        exitMenuItem.addActionListener(e -> {
            frame.dispose();
            System.exit(0);
        });

        // Submit enetry
        submitMenuItem = new JMenuItem("Predaj");
        submitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.CTRL_MASK));
        submitMenuItem.addActionListener(e -> submit());

        // Add menu items to the main menu
        mainMenu.add(submitMenuItem);
        mainMenu.add(exitMenuItem);

        label = new JLabel("Test");
        questionLabel = new JLabel("Pitanje");
        questionEntry = new JTextField();

        answerALabel = new JLabel("Odgovor A:");
        answerAEntry = new JTextField();
        answerBLabel = new JLabel("Odgovor B");
        answerBEntry = new JTextField();
        answerCLabel = new JLabel("Odgovor C");
        answerCEntry = new JTextField();
        answerDLabel = new JLabel("Odgovor D");
        answerDEntry = new JTextField();

        buttonGroup = new ButtonGroup();
        ansARadio = new JRadioButton("A");
        ansARadio.setMnemonic(KeyEvent.VK_A);
        ansBRadio = new JRadioButton("B");
        ansBRadio.setMnemonic(KeyEvent.VK_B);
        ansCRadio = new JRadioButton("C");
        ansCRadio.setMnemonic(KeyEvent.VK_C);
        ansDRadio = new JRadioButton("D");
        ansDRadio.setMnemonic(KeyEvent.VK_D);
        buttonGroup.add(ansARadio);
        buttonGroup.add(ansBRadio);
        buttonGroup.add(ansCRadio);
        buttonGroup.add(ansDRadio);

        // Define type of answer
        typeOfAnswerGroup = new ButtonGroup();
        ansABCD = new JRadioButton("ABCD");
        ansABCD.setMnemonic(KeyEvent.VK_1);
        ansType = new JRadioButton("Nadopiši");
        ansType.setMnemonic(KeyEvent.VK_2);
        ansLong = new JRadioButton("Produženi");
        ansLong.setMnemonic(KeyEvent.VK_3);
        typeOfAnswerGroup.add(ansABCD);
        typeOfAnswerGroup.add(ansType);
        typeOfAnswerGroup.add(ansLong);
        ansABCD.setSelected(true);

        addPicatureButton = new JButton("Dodaj sliku");
        picatureLabel = new JLabel();
        subjectLabel = new JLabel("Predmet");
        subjectEntry = new JTextField();
        yearLabel = new JLabel("Godina");
        yearEntry = new JTextField();
        // Combo boxes
        subjectComboBox = new JComboBox<>(SUBJECTS);
        yearComboBox = new JComboBox<>(YEARS);

        GridBagLayout layoutTop = new GridBagLayout();
        panelTop.setLayout(layoutTop);
        layoutTop.columnWeights = new double[]{0, 0};
        constraints = new GridBagConstraints();

        constraints = getConstrains(0, 0, 1);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        panelTop.add(questionLabel, constraints);
        panelTop.add(questionEntry, getConstrains(0, 1, 2));


        JPanel typeOfAnswerGrid = new JPanel(new GridLayout(1, 3));
        typeOfAnswerGrid.add(ansABCD);
        typeOfAnswerGrid.add(ansType);
        typeOfAnswerGrid.add(ansLong);
        panelTop.add(typeOfAnswerGrid, getConstrains(0, 2, 2));

        CardLayout cardLayout = new CardLayout();
        JPanel answersCardLayout = new JPanel(cardLayout);

        JPanel ansABCDPanel = new JPanel(new GridBagLayout());

        ansABCDPanel.add(answerALabel, getConstrains(0, 0, 1));
        ansABCDPanel.add(answerAEntry, getConstrains(0, 1, 2));
        ansABCDPanel.add(answerBLabel, getConstrains(0, 2, 1));
        ansABCDPanel.add(answerBEntry, getConstrains(0, 3, 2));
        ansABCDPanel.add(answerCLabel, getConstrains(0, 4, 1));
        ansABCDPanel.add(answerCEntry, getConstrains(0, 5, 2));
        ansABCDPanel.add(answerDLabel, getConstrains(0, 6, 1));
        ansABCDPanel.add(answerDEntry, getConstrains(0, 7, 2));

        JPanel radioGrid = new JPanel(new GridLayout(1, 4));
        radioGrid.add(ansARadio);
        radioGrid.add(ansBRadio);
        radioGrid.add(ansCRadio);
        radioGrid.add(ansDRadio);
        ansABCDPanel.add(radioGrid, getConstrains(0, 8, 2));

        constraints = getConstrains(0, 3, 2);
        constraints.gridheight = 9;
        panelTop.add(ansABCDPanel, constraints);

        answersCardLayout.add(ansABCDPanel);

        // Add answers cards
        constraints = getConstrains(0, 3, 2);
        constraints.gridheight = 11;
        panelTop.add(answersCardLayout, constraints);

        ansABCD.addActionListener(e -> {
            answerType = 0;
        });
        ansType.addActionListener(e -> {
            answerType = 1;
        });

        ansLong.addActionListener(e -> {
            answerType = 2;
        });

        FlowLayout layoutCenter = new FlowLayout(FlowLayout.LEFT, 0, 10);
        JPanel panelAddPicature = new JPanel(layoutCenter);
        //panelAddPicature.add(picatureLabel);

        addPicatureButton.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(new File("c:\\Users\\Marko\\Pictures"));
            int returnVal = fc.showOpenDialog(frame);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println(fc.getSelectedFile().getName());
                try {
                    BufferedImage img = ImageIO.read(fc.getSelectedFile());
                    Image i = img.getScaledInstance(100, 100, Image.SCALE_AREA_AVERAGING);
                    ImageIcon icn = new ImageIcon(i);
                    picatureLabel.setIcon(icn);
                    imageAdded = true;
                    imageURI = fc.getSelectedFile().getAbsolutePath();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        panelAddPicature.add(addPicatureButton);

        GridBagLayout layoutBottom = new GridBagLayout();
        JPanel panelBottom = new JPanel(layoutBottom);
        constraints = getConstrains(0, 5, 1);
        constraints.weightx = .5;
        panelBottom.add(subjectLabel, constraints);

        constraints = getConstrains(1, 5, 1);
        constraints.anchor = GridBagConstraints.PAGE_END;
        constraints.weightx = .5;
        panelBottom.add(yearLabel, constraints);

        constraints = getConstrains(0, 6, 1);
        constraints.weightx = .5;
        panelBottom.add(subjectComboBox, constraints);

        constraints = getConstrains(1, 6, 1);
        constraints.weightx = .5;
        panelBottom.add(yearComboBox, constraints);

        panelTop.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelTop.setPreferredSize(new Dimension(500, 450));
        panelAddPicature.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelBottom.setBorder(new EmptyBorder(10, 10, 10, 10));
        picatureLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

        frame.setJMenuBar(menuBar);
        frame.add(panelTop, BorderLayout.PAGE_START);
        frame.add(picatureLabel, BorderLayout.LINE_START);
        frame.add(panelAddPicature, BorderLayout.LINE_END);
        frame.add(panelBottom, BorderLayout.PAGE_END);
        frame.pack();
        frame.setVisible(true);
    }

    private void submit() {
        System.out.println("Predano");
        DatabaseEnetry entry = new DatabaseEnetry(getSubject(), getYear(), questionEntry.getText(),
                answerAEntry.getText(), answerBEntry.getText(), answerCEntry.getText(), answerDEntry.getText(),
                getCorrectAns(), getImage(), answerType);
        HashMap<String, Object> map = entry.toMap();
        System.out.println(map.toString());

        //WriteToFileTest.write(map);

        questionEntry.setText("");
        answerAEntry.setText("");
        answerBEntry.setText("");
        answerCEntry.setText("");
        answerDEntry.setText("");
        picatureLabel.setIcon(null);
        imageAdded = false;
        buttonGroup.clearSelection();

        SwingWorkerUploader swingWorkerUploader = new SwingWorkerUploader(entry, firestore);
        swingWorkerUploader.execute();
    }

    private String getYear() {
        return yearComboBox.getItemAt(yearComboBox.getSelectedIndex());
    }

    private String getSubject() {
        return subjectComboBox.getItemAt(subjectComboBox.getSelectedIndex());
    }

    private String getImage() {
        if (imageAdded) {
            return imageURI;
        }
        return "null";
    }

    private int getCorrectAns() {
        AtomicInteger selected = new AtomicInteger();
        buttonGroup.getElements().asIterator().forEachRemaining(c -> {
            if (c.isSelected()) {
                switch (c.getText()) {
                    case "B" -> selected.set(1);
                    case "C" -> selected.set(2);
                    case "D" -> selected.set(3);
                    default -> selected.set(0);
                }
            }
        });
        return selected.get();
    }

    private GridBagConstraints getConstrains(int x, int y, int width) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridwidth = width;
        constraints.gridheight = 1;
        constraints.weightx = 0.5;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(10, 0, 0, 0);
        return constraints;
    }


    public static void main(String args[]) {
        System.out.println("Test");

        new DatabaseGUI();

    }
}
