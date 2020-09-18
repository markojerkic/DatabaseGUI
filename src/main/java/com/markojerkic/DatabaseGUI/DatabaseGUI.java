package com.markojerkic.DatabaseGUI;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseGUI {

    // Google Firebase variables
    private FileInputStream inputStream;
    private GoogleCredentials googleCredentials;
    private final FirebaseOptions firebaseOptions;
    private final Firestore firestore;
    private final Bucket bucket;

    private final JFrame frame;
    private GridBagConstraints constraints;

    // Elements

    // Top panel
    private final JLabel questionLabel;
    private final JTextField questionEntry;
    private final JLabel answerALabel;
    private final JTextField answerAEntry;
    private final JLabel answerBLabel;
    private final JTextField answerBEntry;
    private final JLabel answerCLabel;
    private final JTextField answerCEntry;
    private final JLabel answerDLabel;
    private final JTextField answerDEntry;
    // Center panel
    private final JButton addImageButton;
    private final JButton addAnswerImageButton;
    private final JLabel pictureLabel;
    // Bottom panel
    private final JLabel subjectLabel;
    private final JComboBox<String> subjectComboBox;
    private final JLabel yearLabel;
    private final JComboBox<String> yearComboBox;

    // Question number
    private final JSpinner questionNumberJSpinner;

    // Type of answer
    private final ButtonGroup typeOfAnswerGroup;
    private final JRadioButton ansABCD;
    private final JRadioButton ansType;
    private final JRadioButton ansLong;

    // Answer buttons
    private final ButtonGroup buttonGroup;
    private final JRadioButton ansARadio;
    private final JRadioButton ansBRadio;
    private final JRadioButton ansCRadio;
    private final JRadioButton ansDRadio;

    // Menu variables
    private final JMenuBar menuBar;
    private final JMenu mainMenu;
    private final JMenuItem exitMenuItem;
    private final JMenuItem submitMenuItem;
    private final JMenuItem addPhotoMenuItem;
    private final JMenuItem previousQuestionMenuItem;
    private final JMenuItem nextQuestionMenuItem;
    private final JMenuItem cancelUpdateMenuItem;

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
    private String[] years;

    // Image state can be approved,disapproved and choosing
    // If an image is chosen than it is stored in chosenBufferedImage
    private ImageChooseState showImageState = ImageChooseState.CHOOSING;
    private BufferedImage chosenBufferedImage;
    // Answer image
    private final ImageChooseState showAnswerImageState = ImageChooseState.CHOOSING;
    private BufferedImage chosenBufferedAnswerImage;
    // Arrays of images
    private final BufferedImage[] bufferedImagesArray = new BufferedImage[]{chosenBufferedImage, chosenBufferedAnswerImage};

    // Program can be in upload and download state
    // When user uses keyboard shortcuts, using upload state we can determine if we want to read from database
    // or use data already downloaded
    private UploadState uploadState = UploadState.UPLOADING;

    // List of question from the database
    private ArrayList<DatabaseEnetry> databaseQuestions;
    private int lastQuestionCounter = 0;

    public DatabaseGUI() {

        // Read the available subjects from a local CSV file
        getSubjectsAndYears();

        // Getting the admin Firebase credentials as shown in official tutorial
        try {
            inputStream = new FileInputStream("C:\\Users\\marko\\Documents\\DatabaseGUIAdmin\\admin_sdk.json");
            googleCredentials = GoogleCredentials.fromStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        firebaseOptions = new FirebaseOptions.Builder()
                .setCredentials(googleCredentials)
                .setDatabaseUrl("https://drzavna-matura-1fbe7.firebaseio.com")
                .setStorageBucket("drzavna-matura-1fbe7.appspot.com")
                .build();
        FirebaseApp.initializeApp(firebaseOptions);
        firestore = FirestoreClient.getFirestore();
        // Bucket for firebase storage to upload images
        bucket = StorageClient.getInstance().bucket();

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
        // Exit button, accessed by the shortcut crt + Q
        exitMenuItem = new JMenuItem("Izlazak");
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        exitMenuItem.addActionListener(e -> {
            // Shutdown the main frame and exit the java app
            frame.dispose();
            System.exit(0);
        });

        // Submit entry menu item
        submitMenuItem = new JMenuItem("Predaj");
        submitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.CTRL_MASK));
        submitMenuItem.addActionListener(e -> submit());

        // Add photo menu item
        addPhotoMenuItem = new JMenuItem("Dodaj sliku");
        addPhotoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        addPhotoMenuItem.addActionListener(e -> choosePhoto(0));

        // Previous question menu item
        previousQuestionMenuItem = new JMenuItem("Proslo pitanje");
        previousQuestionMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.ALT_MASK));
        previousQuestionMenuItem.addActionListener(e -> {
            if (uploadState == UploadState.UPLOADING) {
                // Read from database
                try {
                    // Remove previous values from list
                    databaseQuestions = new ArrayList<>();
                    // Query the database
                    firestore.collection("pitanja").get()
                            .get().getDocuments().stream()
                            .filter(doc -> doc.get("year").toString().equals(getYear()) &&
                                    doc.get("subject").toString().equals(getSubject()))
                            .forEach(doc -> {
                                databaseQuestions.add(new DatabaseEnetry(doc.getData(), doc.getId()));
                                lastQuestionCounter++;
                    });
                    showPreviousQuestion();
                    uploadState = UploadState.DOWNLOADING;
                } catch (InterruptedException | ExecutionException interruptedException) {
                    interruptedException.printStackTrace();
                }
            } else {
                showPreviousQuestion();
            }
        });

        nextQuestionMenuItem = new JMenuItem("Sljedece pitanje");
        nextQuestionMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK));
        nextQuestionMenuItem.addActionListener(e -> {
            if (uploadState == UploadState.DOWNLOADING) {
                showNextQuestion();
            }
        });

        // Change status to download so that when submitting it does not update value of the question,
        // but it adds a new question
        cancelUpdateMenuItem = new JMenuItem("Odustani od popravka");
        cancelUpdateMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.ALT_MASK));
        cancelUpdateMenuItem.addActionListener(e -> {
            if (uploadState == UploadState.DOWNLOADING) {
                uploadState = UploadState.UPLOADING;
            }
        });

        // Add menu items to the main menu
        mainMenu.add(submitMenuItem);
        mainMenu.add(new JSeparator());
        mainMenu.add(addPhotoMenuItem);
        mainMenu.add(previousQuestionMenuItem);
        mainMenu.add(nextQuestionMenuItem);
        mainMenu.add(cancelUpdateMenuItem);
        mainMenu.add(new JSeparator());
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
        ansType = new JRadioButton("Nadopisi");
        ansType.setMnemonic(KeyEvent.VK_2);
        ansLong = new JRadioButton("Produzeni");
        ansLong.setMnemonic(KeyEvent.VK_3);
        typeOfAnswerGroup.add(ansABCD);
        typeOfAnswerGroup.add(ansType);
        typeOfAnswerGroup.add(ansLong);
        ansABCD.setSelected(true);

        // Label and button for adding a photo for the question
        // The photo can be used as the question or th answer
        addImageButton = new JButton("Dodaj sliku");
        addAnswerImageButton = new JButton("Dodaj sliku odgovora");
        pictureLabel = new JLabel();

        // Labels and combo boxes for choosing the year and the subject of the question
        subjectLabel = new JLabel("Predmet");
        yearLabel = new JLabel("Godina");
        // Combo boxes
        subjectComboBox = new JComboBox<>(subjects);
        yearComboBox = new JComboBox<>(years);

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

        addImageButton.addActionListener(e -> {
            choosePhoto(0);
        });
        addAnswerImageButton.addActionListener(e-> {
            choosePhoto(1);
        });
        panelAddPicature.add(addImageButton);
        panelAddPicature.add(addAnswerImageButton);

        GridBagLayout layoutBottom = new GridBagLayout();
        JPanel panelBottom = new JPanel(layoutBottom);

        // Choose question number
        questionNumberJSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        constraints = getConstrains(0, 5, 1);
        panelBottom.add(questionNumberJSpinner, constraints);

        constraints = getConstrains(0, 6, 1);
        constraints.weightx = .5;
        panelBottom.add(subjectLabel, constraints);

        constraints = getConstrains(1, 6, 1);
        constraints.anchor = GridBagConstraints.PAGE_END;
        constraints.weightx = .5;
        panelBottom.add(yearLabel, constraints);

        constraints = getConstrains(0, 7, 1);
        constraints.weightx = .5;
        panelBottom.add(subjectComboBox, constraints);

        constraints = getConstrains(1, 7, 1);
        constraints.weightx = .5;
        panelBottom.add(yearComboBox, constraints);

        panelTop.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelTop.setPreferredSize(new Dimension(500, 450));
        panelAddPicature.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelBottom.setBorder(new EmptyBorder(10, 10, 10, 10));
        pictureLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

        frame.setJMenuBar(menuBar);
        frame.add(panelTop, BorderLayout.PAGE_START);
        frame.add(pictureLabel, BorderLayout.LINE_START);
        frame.add(panelAddPicature, BorderLayout.LINE_END);
        frame.add(panelBottom, BorderLayout.PAGE_END);
        frame.pack();
        frame.setVisible(true);
    }

    private void showNextQuestion() {
        if (databaseQuestions != null && lastQuestionCounter < databaseQuestions.size()-1) {
            lastQuestionCounter++;
            DatabaseEnetry de = databaseQuestions.get(lastQuestionCounter);

            // Set all fields
            setFields(de);
        } else {
            uploadState = UploadState.UPLOADING;
            lastQuestionCounter = 0;
        }
    }

    private void showPreviousQuestion() {
        if (databaseQuestions != null && lastQuestionCounter != 0) {
            lastQuestionCounter--;
            DatabaseEnetry de = databaseQuestions.get(lastQuestionCounter);

            // Set all fields
            setFields(de);
        } else {
            uploadState = UploadState.UPLOADING;
            lastQuestionCounter = 0;
        }
    }

    private void setFields(DatabaseEnetry de) {
        questionEntry.setText(de.getQuestion());
        answerAEntry.setText(de.getAnsA());
        answerBEntry.setText(de.getAnsB());
        answerCEntry.setText(de.getAnsC());
        answerDEntry.setText(de.getAnsD());
        questionNumberJSpinner.setValue(de.getQuestionNumber());
        switch (de.getTypeOfAnswer()) {
            case 0 -> ansABCD.setSelected(true);
            case 1 -> ansType.setSelected(true);
            case 2 -> ansLong.setSelected(true);
        }
        switch (de.getCorrectAns()) {
            case 0: ansARadio.setSelected(true);
            case 1: ansBRadio.setSelected(true);
            case 2: ansCRadio.setSelected(true);
            case 3: ansDRadio.setSelected(true);
        }

    }

    // Open a JFileChooser window in which the user will choose which picture he wants to upload
    // questionOrAnswerImage -> if 0 (question image), else (answer image)
    private void choosePhoto(int questionOrAnswerImage) {
        JFileChooser fc = new JFileChooser(new File("c:\\Users\\Marko\\Pictures"));
        int returnVal = fc.showOpenDialog(frame);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            System.out.println(fc.getSelectedFile().getName());
            try {
                BufferedImage img = ImageIO.read(fc.getSelectedFile());
                Dimension dim = choosePhotoUploadDimensions(img, 1200);
                // Resize the image
                Image i = img.getScaledInstance(dim.width, dim.height, Image.SCALE_SMOOTH);
                bufferedImagesArray[questionOrAnswerImage] = new BufferedImage(dim.width, dim.height,
                BufferedImage.TYPE_INT_RGB);

                Graphics2D g2d = bufferedImagesArray[questionOrAnswerImage].createGraphics();
                g2d.drawImage(i, 0, 0, null);
                g2d.dispose();

                ImageIcon icn = new ImageIcon(bufferedImagesArray[questionOrAnswerImage]);
                System.out.println(bufferedImagesArray[questionOrAnswerImage].getRaster().getDataBuffer().toString());
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
            pictureLabel.setText("Slika dodana");
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
        double ratio = (float) width / (float) height;
        // New values for width and height which will be calculated
        int nWidth, nHeight;
        if (width >= height) {
            nWidth = max;
            nHeight = (int) (nWidth / ratio);
        } else {
            nHeight = max;
            nWidth = (int) (nHeight * ratio);
        }

        return new Dimension(nWidth, nHeight);

    }

    private void getSubjectsAndYears() {
        String subjectsPath = "C:\\Users\\marko\\Documents\\DatabaseGUIAdmin\\subjects.csv";
        String yearsPath = "C:\\Users\\marko\\Documents\\DatabaseGUIAdmin\\years.csv";
        try (BufferedReader reader = new BufferedReader(new FileReader(subjectsPath))) {
            String line = reader.readLine();
            if (line != null) {
                String[] temp = line.split(",");
                this.subjects = temp;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(yearsPath))) {
            String line = reader.readLine();
            if (line != null) {
                String[] temp = line.split(",");
                this.years = temp;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void submit() {
        if (uploadState == UploadState.DOWNLOADING) {
            DatabaseEnetry updateQuestion = databaseQuestions.get(lastQuestionCounter);
            updateQuestion(updateQuestion);
        } else {
            // Create an instance of the DataEntry class
            DatabaseEnetry entry = new DatabaseEnetry(getSubject(), getYear(), questionEntry.getText(),
                    answerAEntry.getText(), answerBEntry.getText(), answerCEntry.getText(), answerDEntry.getText(),
                    getCorrectAns(), bufferedImagesArray[0], answerType,
                    (Integer) questionNumberJSpinner.getValue(), bufferedImagesArray[1]);
            // Get a hash map of the entry
            HashMap<String, Object> map = entry.toMap();

            // Create an instance of swing worker which will upload the entry to the Firebase database
            SwingWorkerUploader swingWorkerUploader = new SwingWorkerUploader(entry, firestore, bucket);
            swingWorkerUploader.execute();

            // Clear all the fields
            resetFields();
        }
    }

    private void updateQuestion(DatabaseEnetry updateQuestion) {
        // Get id of question which you want to update
        String id = updateQuestion.getId();
        // Update all the fields
        updateQuestion.setSubject(getSubject());
        updateQuestion.setYear(getYear());
        updateQuestion.setQuestion(questionEntry.getText());
        updateQuestion.setAnsA(answerAEntry.getText());
        updateQuestion.setAnsB(answerBEntry.getText());
        updateQuestion.setAnsC(answerCEntry.getText());
        updateQuestion.setAnsD(answerDEntry.getText());
        updateQuestion.setCorrectAns(getCorrectAns());
        updateQuestion.setTypeOfAnswer(answerType);
        // Get instance of swing worker, execute the update
        SwingWorkerUploader swingWorkerUploader = new SwingWorkerUploader(updateQuestion, firestore, true);
        swingWorkerUploader.execute();
        // Change state back to upload
        uploadState = UploadState.UPLOADING;
        // Reset all the fields
        resetFields();

    }

    private void resetFields() {
        // Reset all the fields
        questionEntry.setText("");
        answerAEntry.setText("");
        answerBEntry.setText("");
        answerCEntry.setText("");
        answerDEntry.setText("");
        pictureLabel.setText("");
        questionNumberJSpinner.setValue((int) questionNumberJSpinner.getValue() + 1);
        imageAdded = false;
        bufferedImagesArray[0] = null;
        bufferedImagesArray[1] = null;
        buttonGroup.clearSelection();
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


    public static void main(String[] args) {
        new DatabaseGUI();

    }
}
