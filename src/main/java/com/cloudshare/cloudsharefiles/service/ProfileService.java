package com.cloudshare.cloudsharefiles.service;

import org.springframework.stereotype.Service;

import com.cloudshare.cloudsharefiles.document.ProfileDocument;
import com.cloudshare.cloudsharefiles.dto.ProfileDocumentDto;
import com.cloudshare.cloudsharefiles.repository.ProfileRepository;
import com.mongodb.DuplicateKeyException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

import javax.management.RuntimeErrorException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final ProfileRepository profileRepository;
   
    public ProfileDocumentDto createProfile(ProfileDocumentDto profileDocumentDto){
        log.info("Received profile DTO to save: {}", profileDocumentDto);
        if(profileRepository.existsByClerkId(profileDocumentDto.getClerkId())){
        return updateProfile(profileDocumentDto);
    }

        // Convert DTO -> Entity
        ProfileDocument profile = ProfileDocument.builder()
                .id(profileDocumentDto.getId())           // include the generated ID
                .clerkId(profileDocumentDto.getClerkId())
                .email(profileDocumentDto.getEmail())
                .firstname(profileDocumentDto.getFirstname())
                .lastname(profileDocumentDto.getLastname())
                .photoUrl(profileDocumentDto.getPhotoUrl())
                .credits(5)                   // default credits
                .createdAt(Instant.now())     // creation timestamp
                .build();

        // Save entity
        profile = profileRepository.save(profile);
       

        // Convert Entity -> DTO
        ProfileDocumentDto savedDto = ProfileDocumentDto.builder()
                .id(profile.getId())           
                .clerkId(profile.getClerkId())
                .email(profile.getEmail())
                .firstname(profile.getFirstname())
                .lastname(profile.getLastname())
                .photoUrl(profile.getPhotoUrl())
                .credits(profile.getCredits())
                .createdAt(profile.getCreatedAt())
                .build();

        log.info("Returning saved profile DTO: {}", savedDto);
        return savedDto;
    }
    public ProfileDocumentDto updateProfile(ProfileDocumentDto profileDocumentDto) {
    // Find existing profile
    ProfileDocument existingProfile = profileRepository.findByClerkId(profileDocumentDto.getClerkId());

    if (existingProfile != null) {
        // Update fields only if non-null and non-empty
        if (profileDocumentDto.getEmail() != null && !profileDocumentDto.getEmail().isEmpty()) {
            existingProfile.setEmail(profileDocumentDto.getEmail());
        }
        if (profileDocumentDto.getFirstname() != null && !profileDocumentDto.getFirstname().isEmpty()) {
            existingProfile.setFirstname(profileDocumentDto.getFirstname());
        }
        if (profileDocumentDto.getLastname() != null && !profileDocumentDto.getLastname().isEmpty()) {
            existingProfile.setLastname(profileDocumentDto.getLastname());
        }
        if (profileDocumentDto.getPhotoUrl() != null && !profileDocumentDto.getPhotoUrl().isEmpty()) {
            existingProfile.setPhotoUrl(profileDocumentDto.getPhotoUrl());
        }

        // Save updated entity
        profileRepository.save(existingProfile);

        // Return updated DTO
        return ProfileDocumentDto.builder()
                .id(existingProfile.getId())
                .clerkId(existingProfile.getClerkId())
                .email(existingProfile.getEmail())
                .firstname(existingProfile.getFirstname())
                .lastname(existingProfile.getLastname())
                .credits(existingProfile.getCredits())
                .photoUrl(existingProfile.getPhotoUrl())
                .createdAt(existingProfile.getCreatedAt())
                .build();
    } else {
        // Profile not found, return null or throw exception
        return null; // or throw new RuntimeException("Profile not found");
    }
}

    public boolean existsByClerkId(String clerkId){
        return profileRepository.existsByClerkId(clerkId);
    }
    public void deleteProfile(String clerkId){
        ProfileDocument existingProfile=profileRepository.findByClerkId(clerkId);
        if(existingProfile!=null){
            profileRepository.delete(existingProfile);
        }
    }
    public ProfileDocument  getCurrentProfile(){
          if(SecurityContextHolder.getContext().getAuthentication()==null){
            throw new UsernameNotFoundException("User not authenticated");
          }
          String clerkId=SecurityContextHolder.getContext().getAuthentication().getName();
          return profileRepository.findByClerkId(clerkId);
    }
    
   
}
