package com.cloudshare.cloudsharefiles.controller;

import org.springframework.web.bind.annotation.RestController;
import com.cloudshare.cloudsharefiles.dto.ProfileDocumentDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import lombok.RequiredArgsConstructor;
import com.cloudshare.cloudsharefiles.service.ProfileService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    private final ProfileService profileService;
    @PostMapping("/register")
    public ResponseEntity<?> registerProfile(@RequestBody ProfileDocumentDto profileDocumentDto){
        HttpStatus status= profileService.existsByClerkId(profileDocumentDto.getClerkId())?
        HttpStatus.OK:HttpStatus.CREATED;
        ProfileDocumentDto savedProfile=profileService.createProfile(profileDocumentDto);
        log.info("Saved Profile: {}", savedProfile);
        return ResponseEntity.status(status).body(savedProfile);
    }
    
}
