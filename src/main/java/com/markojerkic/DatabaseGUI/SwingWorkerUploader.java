package com.markojerkic.DatabaseGUI;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;

import javax.swing.*;
import java.util.concurrent.ExecutionException;

class SwingWorkerUploader extends SwingWorker<Integer, String> {

    private DatabaseEnetry enetry;
    private Firestore firestore;

    public SwingWorkerUploader (DatabaseEnetry enetry, Firestore firestore) {
        this.enetry = enetry;
        this.firestore = firestore;
    }

    @Override
    protected Integer doInBackground() {
        DocumentReference ref = firestore.collection("pitanja").document();
        ApiFuture<WriteResult> result = ref.set(enetry.toMap());

        try {
            System.out.println("Update time : " + result.get().getUpdateTime().toString());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;

    }

    @Override
    protected void done() {
        super.done();
        System.out.println("Done");
    }
}
