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
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseGUI {

    // Google Firebase variables
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

    // Answer buttons
    private ButtonGroup buttonGroup;
    private JRadioButton ansARadio;
    private JRadioButton ansBRadio;
    private JRadioButton ansCRadio;
    private JRadioButton ansDRadio;

    // Menu variables
    private JMenuBar menuBar;
    private JMenu mainMenu;
    private JMenuItem exitMenuItem;
    private JMenuItem submitMenuItem;
    private JMenuItem addPhotoMenuItem;

    /*
    Type of answer - ABCD (multiple choice), type your answer and show your work
    When the user has to type their own answer, we can just compare it to a string,
    while when the answer is "show your work", we cannot easily check if the answer is correct.
    In this case we will have to just show an image of a correct procedure and we'll let the user decide
    if their work is correct.
     */
    private int answerType = 0;
    private boolean imageAdded = false;
    private String imageURI;

    // Subjects and years - the most basic categories by which the users will search the questions
    // The subjects are read from a local CSV file, while the years are stored in an array, at least for now
    private String[] subjects;
    private String[] YEARS = new String[] {"2019./20.", "2018./19.", "2017./28."};

    // Image state can be approved,disapprovedd and choosing
    // If an image is chosen than it is stored in chosenBufferedImage
    private ImageChooseState showImageState = ImageChooseState.CHOOSING;
    private BufferedImage chosenBufferedImage;

    public DatabaseGUI() {

        // Read the available subjects from a local CSV file
        subjects = getSubjects();

        // Getting the admin Firebase credentials as shown in official tutorial
        try {
            inputStream = new FileInputStream("C:\\Users\\marko\\Documents\\DatabaseGUIAdmin\\admin_sdk.json");
            googleCredentials = GoogleCredentials.fromStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        firebaseOptions = new FirebaseOptions.Builder().setCredentials(googleCredentials)
                .setDatabaseUrl("https://drzavna-matura-1fbe7.firebaseio.com").build();
        FirebaseApp.initializeApp(firebaseOptions);
        firestore = FirestoreClient.getFirestore();

        // Setting the main frame
        frame = new JFrame("Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // The top pannel
        JPanel panelTop = new JPanel();

        // Added a menu bar, as it is the easiest way to add keyboard shortcuts in Swing in my experience
        menuBar = new JMenuBar();
        mainMenu = new JMenu("Menu");
        mainMenu.setMnemonic(KeyEvent.VK_M);
        menuBar.add(mainMenu);
        // Exit button, accesed by the shortcut crt + Q
        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        exitMenuItem.addActionListener(e -> {
            // Shutdown the main frame and exit the java app
            frame.dispose();
            System.exit(0);
        });

        // Submit enetry menu item
        submitMenuItem = new JMenuItem("Predaj");
        submitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.CTRL_MASK));
        submitMenuItem.addActionListener(e -> submit());

        // Add photo menu intem
        addPhotoMenuItem = new JMenuItem("Dodaj sliku");
        addPhotoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        addPhotoMenuItem.addActionListener(e -> choosePhoto());

        // Add menu items to the main menu
        mainMenu.add(submitMenuItem);
        mainMenu.add(addPhotoMenuItem);
        mainMenu.add(exitMenuItem);

        // Label and text field for adding the question
        questionLabel = new JLabel("Pitanje");
        questionEntry = new JTextField();

        // Labels and the text fields of the answers - A through D
        answerALabel = new JLabel("Odgovor A:");
        answerAEntry = new JTextField();
        answerBLabel = new JLabel("Odgovor B");
        answerBEntry = new JTextField();
        answerCLabel = new JLabel("Odgovor C");
        answerCEntry = new JTextField();
        answerDLabel = new JLabel("Odgovor D");
        answerDEntry = new JTextField();

        // Button group for choosing the correct answer
        // Each button has been asigned a keyboard shortcut
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

        /*
        Define type of answer
        Three types of answers: ABCD (multiple choice), type your own answer, and show your work
        When the "type your own answer" is picked, then only answer A is submitted to the database
        "Show your work" category is not yet finished
        */

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

        // Label and button for adding a photo for the question
        // The photo can be used as the question or th answer
        addPicatureButton = new JButton("Dodaj sliku");
        picatureLabel = new JLabel();

        // Labels and combo boxes for choosing the year and the subject of the question
        subjectLabel = new JLabel("Predmet");
        yearLabel = new JLabel("Godina");
        // Combo boxes
        subjectComboBox = new JComboBox<>(subjects);
        yearComboBox = new JComboBox<>(YEARS);

        // Top panel is set in a grid bag layout
        GridBagLayout layoutTop = new GridBagLayout();
        panelTop.setLayout(layoutTop);
        layoutTop.columnWeights = new double[]{0, 0};

        // The constrains are initialised once, but are updated before adding each element, as most
        // share a lot of features, only having slight differences which are defined later
        constraints = new GridBagConstraints();

        // Defining the positions of the question label and text field
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
            choosePhoto();
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

    // Open a JFileChooser window in which the user will choose which picture he wants to upload
    private void choosePhoto() {
        JFileChooser fc = new JFileChooser(new File("c:\\Users\\Marko\\Pictures"));
        int returnVal = fc.showOpenDialog(frame);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            System.out.println(fc.getSelectedFile().getName());
            try {
                BufferedImage img = ImageIO.read(fc.getSelectedFile());
                Dimension dim = choosePhotoUploadDimensions(img, 1200);
                Image i = img.getScaledInstance(dim.width, dim.height, Image.SCALE_SMOOTH);
                chosenBufferedImage = (BufferedImage) i;
                ImageIcon icn = new ImageIcon(i);
                showImage(icn);
                imageAdded = true;
                imageURI = fc.getSelectedFile().getAbsolutePath();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    // Creates a new JFrame in which the photo will be shown
    // and the user will choose if he wants to upload it
    private void showImage(ImageIcon icon) {
        JFrame imageFrame = new JFrame("Image");
        imageFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel nPanel = new JPanel();
        imageFrame.add(nPanel);

        // Create label for the icon
        JLabel showImageLabel = new JLabel();
        showImageLabel.setIcon(icon);
        nPanel.add(showImageLabel);

        // Create menu for quick confirm action
        JMenuBar showImageMenuBar = new JMenuBar();
        JMenu showImageMenu = new JMenu("Menu");
        showImageMenuBar.add(showImageMenu);
        // Menu item for approval
        JMenuItem confirm = new JMenuItem("Potvrdi");
        confirm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.CTRL_MASK));
        // If the item is chosen, then the state field is switched to APPROVE and the frame is closed
        confirm.addActionListener(e -> {
            System.out.println("Approved");
            showImageState = ImageChooseState.APPROVED;
            imageFrame.dispose();
        });
        // Menu item for disapproval
        JMenuItem disapprove = new JMenuItem("Otkazi");
        disapprove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ActionEvent.CTRL_MASK));
        disapprove.addActionListener(e -> {
            System.out.println("Nooooooooope");
            showImageState = ImageChooseState.DISAPPROVE;
            imageFrame.dispose();
        });

        // Add items to the menu
        showImageMenu.add(confirm);
        showImageMenu.add(disapprove);

        // Pack and show the frame
        imageFrame.setJMenuBar(showImageMenuBar);
        imageFrame.pack();
        imageFrame.getContentPane().add(BorderLayout.CENTER, nPanel);
        imageFrame.setLocationByPlatform(true);
        imageFrame.setVisible(true);
    }

    // Choose dimension
    // Scale the image so that width and height don't surpass the maximum which is passed
    private Dimension choosePhotoUploadDimensions(BufferedImage img, int max) {
        int height = img.getHeight();
        int width = img.getWidth();
        // Ratio of width to height which will be used to scale the image properly
        double ratio = width / height;
        // New values for width and height which will be calculated
        int nWidth, nHeight;
        nWidth = max;
        nHeight = (int) (nWidth / ratio);

        if (nHeight <= max)
            return new Dimension(nWidth, nHeight);
        return null;

    }

    private String[] getSubjects() {
        ArrayList<String> res = new ArrayList<>();
        String path = "C:\\Users\\marko\\Documents\\DatabaseGUIAdmin\\subjects.csv";
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line = reader.readLine();
            if (line != null) {
                String[] temp = line.split(",");
                return temp;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

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
