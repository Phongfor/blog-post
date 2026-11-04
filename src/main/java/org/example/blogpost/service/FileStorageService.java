package org.example.blogpost.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

public interface FileStorageService {
    String POSTS_FOLDER = "posts";

    record StoredFile(String fileName, String fileUrl){
    }

    StoredFile store(MultipartFile file,String folder);

    void delete(String folder, String fileName);

    void deleteAfterCommit(String folder, Collection<String> fileNames);

    void deleteOnRollback(String folder, Collection<String> fileNames);
}
