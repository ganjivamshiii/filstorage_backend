package com.cloudshare.cloudsharefiles.controller;

import java.io.File;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import java.io.IOException;

import com.cloudshare.cloudsharefiles.service.FileConvertService;

@RestController
@RequestMapping("/convert")
public class FileConvertController {

     private FileConvertService fileConvertService;

    @PostMapping("/file")
public ResponseEntity<Resource> convertFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam("format") String format) throws IOException {

     try {
        File convertedFile = fileConvertService.convertFile(file, format);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(convertedFile));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + convertedFile.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    } catch (Exception e) {
        return ResponseEntity.internalServerError()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(null);
    }
}

@PostMapping("/compress")
public ResponseEntity<Resource> compressFile(
        @RequestParam("file") MultipartFile file) throws IOException {

    File compressed = fileConvertService.compressFile(file);
    InputStreamResource resource = new InputStreamResource(new FileInputStream(compressed));

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + compressed.getName())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
}

}
