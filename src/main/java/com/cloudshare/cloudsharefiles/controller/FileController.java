package com.cloudshare.cloudsharefiles.controller;
import org.apache.catalina.connector.Response;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.cloudshare.cloudsharefiles.document.UserCredits;
import com.cloudshare.cloudsharefiles.dto.FileMetadataDto;
import com.cloudshare.cloudsharefiles.service.FileMetadataService;
import com.cloudshare.cloudsharefiles.service.UserCreditsService;


import io.jsonwebtoken.io.IOException;


import java.util.List;
import java.util.Map;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

@RestController
@RequiredArgsConstructor

@RequestMapping("/files")
@Slf4j
public class FileController {
    
    private final FileMetadataService fileMetadataService;
    private final UserCreditsService userCreditsService;
   
@PostMapping("/upload")
public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile[] files) throws IOException {
    // Debug logging at controller level
    System.out.println("=== CONTROLLER DEBUG ===");
    System.out.println("Number of files received: " + files.length);
    
    for (int i = 0; i < files.length; i++) {
        MultipartFile file = files[i];
        System.out.println("File " + i + ":");
        System.out.println("  - Original filename: '" + file.getOriginalFilename() + "'");
        System.out.println("  - Content type: '" + file.getContentType() + "'");
        System.out.println("  - Size: " + file.getSize());
        System.out.println("  - Empty: " + file.isEmpty());
        System.out.println("  - Name: '" + file.getName() + "'");
        
        // Try to read some bytes to see if there's actual data
        try {
            byte[] bytes = file.getBytes();
            System.out.println("  - Byte array length: " + bytes.length);
            if (bytes.length > 0) {
                System.out.println("  - First few bytes: " + java.util.Arrays.toString(java.util.Arrays.copyOf(bytes, Math.min(10, bytes.length))));
            }
        } catch (Exception e) {
            System.out.println("  - Error reading bytes: " + e.getMessage());
        }
    }
    System.out.println("========================");
    
    // Check if files are actually empty - if so, return error instead of processing
    boolean hasEmptyFiles = java.util.Arrays.stream(files).anyMatch(MultipartFile::isEmpty);
    if (hasEmptyFiles) {
        System.out.println("ERROR: One or more files are empty. Not processing.");
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "No files received or files are empty");
        errorResponse.put("debug", "Check your Postman form-data configuration");
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    Map<String, Object> response = new HashMap<>();
    List<FileMetadataDto> list = fileMetadataService.uploadFiles(files);
    
    UserCredits finalCredits = userCreditsService.getUserCredits();
    
    response.put("files", list);
    response.put("remainingCredits", finalCredits.getCredits());
    return ResponseEntity.ok(response);
}
@GetMapping("/my")
 public ResponseEntity<?> getFileForCurrentUser(){
    List<FileMetadataDto> files =fileMetadataService.getFiles();
    return ResponseEntity.ok(files); }

 @GetMapping("/public/{id}")
     public ResponseEntity<?> getPublicFile(@PathVariable String id){
        System.out.println("files publlic");
        FileMetadataDto file =fileMetadataService.getPublicFile(id);
        return ResponseEntity.ok(file);
     
     }

 @GetMapping("/download/{id}")
  public ResponseEntity<Resource> download(@PathVariable String id) throws IOException{
    System.out.println(" Request processed");
    FileMetadataDto downloadableFile=fileMetadataService.getDownloadableFile(id);
    Path path=Paths.get(downloadableFile.getFileLocation());
    Resource resource;
              try {
                    resource = new UrlResource(path.toUri());
            } catch (MalformedURLException e) {
                    throw new RuntimeException("Invalid file path: " + downloadableFile.getFileLocation(), e);
            }
        return ResponseEntity.ok() 
     .contentType(MediaType.APPLICATION_OCTET_STREAM)
     .header(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=\"" + downloadableFile.getName() + "\"")
     .body(resource);
  }
  @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable String id){
        fileMetadataService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("{id}/toggle-public")
    public ResponseEntity<?> togglePublic(@PathVariable String id){
         System.out.println("chantedfile permisson");
        FileMetadataDto file=fileMetadataService.togglePublic(id);
        return ResponseEntity.ok(file);
    }
     
} 


