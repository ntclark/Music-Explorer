package com.envisionate.musicexplorer;

import android.view.View;

import androidx.documentfile.provider.DocumentFile;

public class itemModel {
    private String name;
    private String itemPath;
    private DocumentFile documentFile = null;
    private View itemImageView = null;
    private View itemTextView = null;
    public itemModel(String name, DocumentFile fileUri) {
        this.name = name;
        itemImageView = null;
        itemTextView = null;
        documentFile = fileUri;
        itemPath = documentFile.getUri().toString();
    }

    public String getName() {
        return name;
    }

    public DocumentFile getDocumentFile() {
        return documentFile;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageView(View view) {
        this.itemImageView = view;
    }

    public View getImageView() {
        return itemImageView;
    }

    public void setTextView(View view) {
        this.itemTextView = view;
    }

    public View getTextView() {
        return itemTextView;
    }

}