package com.markojerkic.DatabaseGUI;

import java.util.HashMap;

public class DatabaseEnetry {
    private String subject;
    private String year;
    private String question;
    private String ansA;
    private String ansB;
    private String ansC;
    private String ansD;
    private int correctAns;
    private String imageURI;
    private int typeOfAnswer;

    public DatabaseEnetry(String subject, String year, String question, String ansA, String ansB, String ansC,
                          String ansD, int correctAns, String imageURI, int typeOfAnswer) {
        this.subject = subject;
        this.year = year;
        this.question = question;
        this.ansA = ansA;
        this.ansB = ansB;
        this.ansC = ansC;
        this.ansD = ansD;
        this.correctAns = correctAns;
        this.imageURI = imageURI;
        this.typeOfAnswer = typeOfAnswer;
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
        map.put("correctAns", this.correctAns);
        map.put("imageURI", this.imageURI);
        map.put("typeOfAnswer", this.typeOfAnswer);

        return map;
    }
}
