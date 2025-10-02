package com.cloudshare.cloudsharefiles.service;


import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import com.cloudshare.cloudsharefiles.dto.FileMetadataDto;
import com.cloudshare.cloudsharefiles.repository.FileMetadataRepository;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.cloudshare.cloudsharefiles.document.FileMetadataDocument;
import com.cloudshare.cloudsharefiles.document.ProfileDocument;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.Optional;




import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FileMetadataService {

    private final ProfileService profileService;
    private final UserCreditsService userCreditsService;
    private final FileMetadataRepository fileMetadataRepository;


   public List<FileMetadataDto> uploadFiles(MultipartFile[] files) {
    ProfileDocument currentProfile = profileService.getCurrentProfile();
    List<FileMetadataDocument> savedFiles = new ArrayList<>();

    if (!userCreditsService.hasEnoughCredits(files.length)) {
        throw new RuntimeException("Not enough Credits to upload files. Please purchase more");
    }

    Path uploadPath = Paths.get("upload").toAbsolutePath().normalize();
    try {
        Files.createDirectories(uploadPath);

        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            
            // More robust extension extraction
            if (originalFilename != null && !originalFilename.trim().isEmpty()) {
                originalFilename = originalFilename.trim();
                int dotIndex = originalFilename.lastIndexOf('.');
                
                // Ensure there's a dot, it's not at the start, and there's content after it
                if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
                    extension = originalFilename.substring(dotIndex).toLowerCase();
                }
            }
            
            // Fallback: try to determine extension from content type if filename extension fails
            if (extension.isEmpty() && file.getContentType() != null) {
                String contentType = file.getContentType().toLowerCase();
                switch (contentType) {
                    case "application/pdf":
                        extension = ".pdf";
                        break;
                    case "text/plain":
                        extension = ".txt";
                        break;
                    case "image/jpeg":
                        extension = ".jpg";
                        break;
                    case "image/png":
                        extension = ".png";
                        break;
                    case "application/msword":
                        extension = ".doc";
                        break;
                    case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                        extension = ".docx";
                        break;
                    // Add more as needed
                }
            }
            
            System.out.println("Final extension: '" + extension + "'");
          

            String filename = UUID.randomUUID().toString() + extension;
             System.out.println("Final extension: '" + extension + "'");
            Path targetLocation = uploadPath.resolve(filename);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            FileMetadataDocument fileMetadata = FileMetadataDocument.builder()
                    .fileLocation(targetLocation.toString())
                    .name(originalFilename)
                    .size(file.getSize())
                    .type(file.getContentType())
                    .clerkId(currentProfile.getClerkId())
                    .isPublic(false)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            userCreditsService.consumeCredits();
            savedFiles.add(fileMetadataRepository.save(fileMetadata));
        }

    } catch (IOException e) {
        throw new RuntimeException("Could not store files", e);
    }

    return savedFiles.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
}

private FileMetadataDto mapToDTO(FileMetadataDocument fileMetadataDocument) {
    return FileMetadataDto.builder()
     .id(fileMetadataDocument.getId())
            .fileLocation(fileMetadataDocument.getFileLocation())
            .name(fileMetadataDocument.getName())
            .size(fileMetadataDocument.getSize())
            .type(fileMetadataDocument.getType())
            .clerkId(fileMetadataDocument.getClerkId())
            .isPublic(fileMetadataDocument.getIsPublic())
            .uploadedAt(fileMetadataDocument.getUploadedAt())
            .build();
}
public List<FileMetadataDto> getFiles(){
    ProfileDocument currentProfile=profileService.getCurrentProfile();
    List<FileMetadataDocument> files=fileMetadataRepository.findByClerkId(currentProfile.getClerkId());
    return files.stream().map(this::mapToDTO).collect(Collectors.toList());
}
public FileMetadataDto getPublicFile(String id){
    Optional<FileMetadataDocument> fileOptional=fileMetadataRepository.findById(id);
    if(fileOptional.isEmpty()|| ! fileOptional.get().getIsPublic()){
        throw new RuntimeException("Unable to get the file");
    }
    FileMetadataDocument document=fileOptional.get();
    return mapToDTO(document);
}
public FileMetadataDto getDownloadableFile(String id){
    FileMetadataDocument file=fileMetadataRepository
    .findById(id)
    .orElseThrow(()-> new RuntimeException("File not Found"));
    return mapToDTO(file);
}
public void deleteFile(String id){
    try{
        ProfileDocument currentProfile=profileService.getCurrentProfile();
        FileMetadataDocument file=fileMetadataRepository.findById(id)
            .orElseThrow(()-> new RuntimeException(" file not found"));
            if(!file.getClerkId().equals(currentProfile.getClerkId())){
                throw new RuntimeException("File is not belong to current user");
            }
          Path filePath= Paths.get(file.getFileLocation());
            Files.deleteIfExists(filePath);
            fileMetadataRepository.deleteById(id);
    }catch(Exception e){
             throw new RuntimeException(" Error deleting file"); 
    }
}
 public FileMetadataDto togglePublic(String id){
                FileMetadataDocument file=fileMetadataRepository.findById(id)
                             .orElseThrow(()-> new RuntimeException("File not found"));
                             file.setIsPublic(!file.getIsPublic());
                             fileMetadataRepository.save(file);
                             return mapToDTO(file);

        }

}
