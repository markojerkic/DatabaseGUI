package com.markojerkic.DatabaseGUI;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DatabaseEnetry {

    private String subject;
    private String year;
    private String question;
    private String ansA;
    private String ansB;
    private String ansC;
    private String ansD;
    private int correctAns;
    private BufferedImage img;
    private String imgName;
    private BufferedImage ansImg;
    private String ansImgName;
    private String superQuestion;
    private BufferedImage superQuestionImage;
    private String superQuestionImageName;
    private int typeOfAnswer;
    private String id;
    private int questionNumber;
    private boolean imgUploaded;
    private String superImageName;
    private String imageName;
    private String ansImageName;
    private File audioFile;
    private boolean audioUploaded;
    private String audioName;

    public DatabaseEnetry(String subject, String year, String question, String ansA, String ansB, String ansC,
                          String ansD, int correctAns, BufferedImage img,
                          int typeOfAnswer, int questionNumber, BufferedImage ansImg,
                          String superQuestion, BufferedImage superQuestionImage, boolean imgUploaded,
                          File audio, boolean audioUploaded) {
        this.subject = subject;
        this.year = year;
        this.question = question;
        this.ansA = ansA;
        this.ansB = ansB;
        this.ansC = ansC;
        this.ansD = ansD;
        this.correctAns = correctAns;
        this.img = img;
        this.typeOfAnswer = typeOfAnswer;
        this.questionNumber = questionNumber;
        this.ansImg = ansImg;
        this.superQuestion = superQuestion;
        this.superQuestionImage = superQuestionImage;
        this.imgUploaded = imgUploaded;
        this.audioFile = audio;
        this.audioUploaded = audioUploaded;
    }

    public DatabaseEnetry(Map<String, Object> map, String id) {
        this.subject = (String) map.get("subject");
        this.year = (String) map.get("year");
        this.question = (String) map.get("question");
        this.ansA = (String) map.get("ansA");
        this.ansB = (String) map.get("ansB");
        this.ansC = (String) map.get("ansC");
        this.ansD = (String) map.get("ansD");
        this.correctAns = ((Long) map.get("correctAns")).intValue();
        try {
            this.imgName = (String) map.get("imageURI");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.ansImgName = (String) map.get("ansImg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.typeOfAnswer = ((Long) map.get("typeOfAnswer")).intValue();
        this.id = id;
        this.questionNumber = ((Long) map.get("questionNumber")).intValue();
    }

    public BufferedImage getImg() {
        return this.img;
    }

    public String getQuestion() {
        return this.question;
    }

    public void setImg(String name) {
        this.imgName = name;
    }

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("subject", this.subject);
        map.put("year", this.year);
        map.put("question", this.question);
        map.put("ansA", this.ansA);
        map.put("ansB", this.ansB);
        map.put("ansC", this.ansC);
        map.put("ansD", this.ansD);
        map.put("questionNumber", this.questionNumber);
        map.put("correctAns", this.correctAns);
        if (imgName != null)
            map.put("imageURI", this.imgName);
        if (ansImgName != null)
            map.put("ansImg", this.ansImgName);
        if (superQuestion != null) map.put("superQuestion", this.superQuestion);
        if (superQuestionImageName != null) map.put("superQuestionImage", this.superQuestionImageName);
        if (audioName != null) map.put("audioName", this.audioName);
        map.put("typeOfAnswer", this.typeOfAnswer);

        return map;
    }

    public int getAns() {
        return this.correctAns;
    }
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnsA() {
        return ansA;
    }

    public void setAnsA(String ansA) {
        this.ansA = ansA;
    }

    public String getAnsB() {
        return ansB;
    }

    public void setAnsB(String ansB) {
        this.ansB = ansB;
    }

    public String getAnsC() {
        return ansC;
    }

    public void setAnsC(String ansC) {
        this.ansC = ansC;
    }

    public String getAnsD() {
        return ansD;
    }

    public void setAnsD(String ansD) {
        this.ansD = ansD;
    }

    public int getCorrectAns() {
        return correctAns;
    }

    public void setCorrectAns(int correctAns) {
        this.correctAns = correctAns;
    }

    public void setImg(BufferedImage img) {
        this.img = img;
    }

    public int getTypeOfAnswer() {
        return typeOfAnswer;
    }

    public void setTypeOfAnswer(int typeOfAnswer) {
        this.typeOfAnswer = typeOfAnswer;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public String getId() {
        return id;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(int questionNumber) {
        this.questionNumber = questionNumber;
    }

    public BufferedImage getAnsImg() {
        return ansImg;
    }

    public void setAnsImg(BufferedImage ansImg) {
        this.ansImg = ansImg;
    }

    public String getAnsImgName() {
        return ansImgName;
    }

    public void setAnsImgName(String ansImgName) {
        this.ansImgName = ansImgName;
    }

    public String getSuperQuestion() {
        return superQuestion;
    }

    public void setSuperQuestion(String superQuestion) {
        this.superQuestion = superQuestion;
    }

    public BufferedImage getSuperQuestionImage() {
        return superQuestionImage;
    }

    public void setSuperQuestionImage(BufferedImage superQuestionImage) {
        this.superQuestionImage = superQuestionImage;
    }

    public String getSuperQuestionImageName() {
        return superQuestionImageName;
    }

    public void setSuperQuestionImageName(String superQuestionImageName) {
        this.superQuestionImageName = superQuestionImageName;
    }

    public boolean isImgUploaded() {
        return imgUploaded;
    }

    public void setImgUploaded(boolean imgUploaded) {
        this.imgUploaded = imgUploaded;
    }



    public String createSuperImageName(){
        if (superImageName == null) {
            superImageName = "super" + this.getSuperQuestion().split(" ").length +
                    this.getSuperQuestion().length()
                    + this.getSuperQuestion().split(" ")[0]
                    + this.getSuperQuestion().split(" ")[this.getSuperQuestion().split(" ").length - 1]
                    + randNameInt();
            setSuperQuestionImageName(superImageName);
        }
        return superImageName;
    }

    public String createImageName() {
        if (imageName == null) {
            return String.valueOf(this.getQuestion().split(" ").length) +
                    this.getQuestion().length()
                    + this.getQuestion().split(" ")[0]
                    + this.getAns()
                    + randNameInt();
        }
        return imageName;
    }



    private String randNameInt() {
        Random rand = new Random();
        return String.valueOf(rand.nextInt());
    }

    public String getSuperImageName() {
        return superImageName;
    }

    public void setSuperImageName(String superImageName) {
        this.superImageName = superImageName;
        setSuperQuestionImageName(superImageName);
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getAnsImageName() {
        return ansImageName;
    }

    public void setAnsImageName(String ansImageName) {
        this.ansImageName = ansImageName;
    }

    public File getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(File audioFile) {
        this.audioFile = audioFile;
    }

    public boolean isAudioUploaded() {
        return audioUploaded;
    }

    public void setAudioUploaded(boolean audioUploaded) {
        this.audioUploaded = audioUploaded;
    }

    public String getAudioName() {
        if (this.audioName == null)
            this.audioName = audioFile.getName()+ randNameInt();
        return this.audioName;
    }

    public void setAudioName(String audioName) {
        this.audioName = audioName;
    }
}
