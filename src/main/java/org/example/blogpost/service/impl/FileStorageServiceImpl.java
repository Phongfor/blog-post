package org.example.blogpost.service.impl;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.blogpost.exception.AppException;
import org.example.blogpost.exception.ErrorCode;
import org.example.blogpost.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final int HEADER_SIZE = 12;

    Path rootDir;

    public FileStorageServiceImpl(@Value("${app.upload-dir:upload}") String uploadDIr){
        this.rootDir = Path.of(uploadDIr).toAbsolutePath().normalize();
    }

    @Override
    public StoredFile store(MultipartFile file,String folder) {
        if(file == null || file.isEmpty()){
            throw new AppException(ErrorCode.INVALID_IMAGE_FILE);
        }
        if(file.getSize() > MAX_FILE_SIZE){
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        String extension = detectExtension(file);
        String fileName = UUID.randomUUID().toString() + "." + extension;
        Path target = resolve(folder,fileName);

        try{
            Files.createDirectories(target.getParent());
            try(InputStream in = file.getInputStream()){
                Files.copy(in, target);
            }
        }catch (IOException e){
            log.error("Failed to store file {}/{}", folder, fileName, e);
            throw new AppException(ErrorCode.FILE_STORAGE_ERROR);
        }
        log.info("File stored: {}/{}", folder, fileName);
        return new StoredFile(fileName, "/uploads/" + folder + "/" + fileName);
    }

    @Override
    public void delete(String folder, String fileName) {
        try{
            Files.deleteIfExists(resolve(folder,fileName));
        }catch (IOException e){
            log.warn("Failed to delete file {}/{}", folder, fileName, e);
        }
    }

    @Override
    public void deleteAfterCommit(String folder, Collection<String> fileNames) {
        deleteWhenTransactionEnds(folder, fileNames, true);
    }

    @Override
    public void deleteOnRollback(String folder, Collection<String> fileNames) {
        deleteWhenTransactionEnds(folder, fileNames, false);
    }

    private void  deleteWhenTransactionEnds(String folder, Collection<String> fileNames, boolean onCommit) {
        List<String> names = List.copyOf(fileNames);
        if(names.isEmpty()){
            return;
        }

        if(!TransactionSynchronizationManager.isSynchronizationActive()){
            if(onCommit){
                names.forEach((name -> delete(folder,name)));
            }
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                boolean shouldDelete = onCommit
                        ? status == STATUS_COMMITTED
                        : status == STATUS_ROLLED_BACK;
                if (shouldDelete) {
                    names.forEach(name -> delete(folder, name));
                }
            }
        });
    }

    private String detectExtension(MultipartFile file) {
        byte[] header = new byte[HEADER_SIZE];
        int read;
        try(InputStream in = file.getInputStream()){
            read = in.readNBytes(header,0,HEADER_SIZE);
        } catch (IOException e) {
            log.error("Failed to read uploaded file", e);
            throw new AppException(ErrorCode.FILE_STORAGE_ERROR);
        }

        if(read < HEADER_SIZE){
            throw new AppException(ErrorCode.INVALID_IMAGE_FILE);
        }

        if (matches(header, 0, 0xFF, 0xD8, 0xFF)) {
            return "jpg";
        }
        if (matches(header, 0, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
            return "png";
        }

        if (matches(header, 0, 0x52, 0x49, 0x46, 0x46) && matches(header, 8, 0x57, 0x45, 0x42, 0x50)) {
            return "webp";
        }
        throw new AppException(ErrorCode.INVALID_IMAGE_FILE);


    }

    private Path resolve(String folder,String fileName) {
        Path path = rootDir.resolve(folder).resolve(fileName).normalize();
        if(!path.startsWith(rootDir)){
            throw new AppException(ErrorCode.FILE_STORAGE_ERROR);
        }
        return path;
    }

    private static boolean matches(byte[] data, int offset, int ... expected){
        for (int i=0; i<expected.length; i++){
            if((data[offset+i] & 0xFF) != expected[i]){
                return false;
            }
        }
        return true;
    }
}
