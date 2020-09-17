package com.markojerkic.DatabaseGUI;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.storage.Bucket;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;

class SwingWorkerUploader extends SwingWorker<Integer, String> {

    private final DatabaseEnetry entry;
    private final Firestore firestore;
    private Bucket bucket;
    private String imgName;
    private String ansImgName;

    // If update, change value at id
    private boolean update = false;

    public SwingWorkerUploader (DatabaseEnetry entry, Firestore firestore, Bucket bucket) {
        this.entry = entry;
        this.firestore = firestore;
        this.bucket = bucket;
        this.update = false;
    }

    public SwingWorkerUploader (DatabaseEnetry entry, Firestore firestore, boolean update) {
        this.update = update;
        this.entry = entry;
        this.firestore = firestore;
    }

    @Override
    protected Integer doInBackground() {
        if (update)
            return update();
        else {
            return addNewValue();
        }

    }

    private int addNewValue() {
        DocumentReference ref = firestore.collection("pitanja").document();
        // Upload question image
        try {
            if (entry.getImg() != null) {
                uploadImage();
                entry.setImg(imgName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Upload answer image if exists
        try {
            if (entry.getAnsImg() != null) {
                uploadAnswerImage();
                entry.setAnsImgName(ansImgName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ApiFuture<WriteResult> result = ref.set(entry.toMap());
        return printSuccessful(result);

    }

    private int update() {
        DocumentReference ref = firestore.collection("pitanja").document(entry.getId());
        ApiFuture<WriteResult> result = ref.set(entry.toMap());
        return printSuccessful(result);
    }

    private int printSuccessful (ApiFuture<WriteResult> result) {
        try {
            System.out.println("Update time : " + result.get().getUpdateTime().toString());
            return 0;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void uploadAnswerImage() throws IOException {
        // File to which the image will be outputted as png which will be uploaded
        ansImgName = "ans" + createImageName();
        File outputFile = new File("C:\\Users\\marko\\Pictures\\databaseGUIImages\\"+ imgName + ".png");
        ImageIO.write(this.entry.getAnsImg(), "png", outputFile);

        bucket.create(ansImgName + ".png", Files.readAllBytes(outputFile.toPath()));

    }

    private void uploadImage() throws IOException {
        // File to which the image will be outputed as png which will be uploaded
        imgName = createImageName();
        File outputFile = new File("C:\\Users\\marko\\Pictures\\databaseGUIImages\\"+ imgName + ".png");
        ImageIO.write(this.entry.getImg(), "png", outputFile);

        bucket.create(imgName + ".png", Files.readAllBytes(outputFile.toPath()));
    }

    private String createImageName() {
        return String.valueOf(this.entry.getQuestion().split(" ").length) +
                this.entry.getQuestion().length()
                + this.entry.getQuestion().split(" ")[0]
                + this.entry.getAns();
    }

    @Override
    protected void done() {
        super.done();
        System.out.println("Done");
    }
}
