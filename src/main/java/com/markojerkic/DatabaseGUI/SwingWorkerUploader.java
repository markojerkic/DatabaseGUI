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

    private DatabaseEnetry enetry;
    private Firestore firestore;
    private Bucket bucket;

    public SwingWorkerUploader (DatabaseEnetry enetry, Firestore firestore, Bucket bucket) {
        this.enetry = enetry;
        this.firestore = firestore;
        this.bucket = bucket;
    }

    @Override
    protected Integer doInBackground() {
        DocumentReference ref = firestore.collection("pitanja").document();
        ApiFuture<WriteResult> result = ref.set(enetry.toMap());

        try {
            uploadImage();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("Update time : " + result.get().getUpdateTime().toString());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;

    }

    private void uploadImage() throws IOException {
        // File to which the image will be outputed as png which will be uploaded
        File outputFile = new File("C:\\Users\\marko\\"+ this.enetry.getQuestion() + ".png");
        ImageIO.write(this.enetry.getImg(), "png", outputFile);

        bucket.create(this.enetry.getQuestion() + ".png", Files.readAllBytes(outputFile.toPath()));
    }

    @Override
    protected void done() {
        super.done();
        System.out.println("Done");
    }
}
