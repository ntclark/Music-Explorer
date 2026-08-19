package com.envisionate.musicinfolders;

import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;

public class filesAndFolders {

        public filesAndFolders(ArrayList<DocumentFile> folders,ArrayList<DocumentFile> files) {
            theFolders = folders;
            theFiles = files;
        }

        private ArrayList<DocumentFile> theFolders;
        private ArrayList<DocumentFile> theFiles;

        public ArrayList<DocumentFile> getFolders() {
            return theFolders;
        }

        public ArrayList<DocumentFile> getFiles() {
            return theFiles;
        }

    }
