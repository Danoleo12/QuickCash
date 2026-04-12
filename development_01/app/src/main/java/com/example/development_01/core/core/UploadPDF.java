package com.example.development_01.core.core;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UploadPDF {

    // Method to upload PDF
    public void uploadPDFToFirebase(Uri fileUri) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("resumes");

        StorageReference fileRef = storageRef.child(System.currentTimeMillis() + "_" + fileUri.getLastPathSegment());

        UploadTask uploadTask = fileRef.putFile(fileUri);

        // Register observers to listen for when the upload is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                System.out.println("Upload failed: " + exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Upload successful
                System.out.println("Upload successful!");

                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUri) {
                        String downloadUrl = downloadUri.toString();
                        System.out.println("Download URL: " + downloadUrl);
                    }
                });
            }
        });
    }
}