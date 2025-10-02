package com.cloudshare.cloudsharefiles.service;

import org.springframework.stereotype.Service;

import com.cloudshare.cloudsharefiles.document.FileMetadataDocument;
import com.cloudshare.cloudsharefiles.document.UserCredits;
import com.cloudshare.cloudsharefiles.dto.FileMetadataDto;
import com.cloudshare.cloudsharefiles.repository.UserCreditsRepository;
import com.cloudshare.cloudsharefiles.service.ProfileService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserCreditsService {

        private final UserCreditsRepository userCreditsRepository;
        private final ProfileService profileService;

        public UserCredits createIntailCredits(String clerkId){
           UserCredits userCredits= UserCredits.builder()
            .clerkId(clerkId)
            .credits(10)
            .plan("BASIC")
            .build();
           return  userCreditsRepository.save(userCredits);
        }
        public UserCredits getUserCredits(String clerkId) {
            return userCreditsRepository.findByClerkId(clerkId)
            .orElseGet(() -> createInitialCredits(clerkId));
           }
private UserCredits createInitialCredits(String clerkId) {
    UserCredits uc = new UserCredits();
    uc.setClerkId(clerkId);
    uc.setCredits(10); // default value
    return userCreditsRepository.save(uc);
}
        public UserCredits getUserCredits(){
                String clerkId=profileService.getCurrentProfile().getClerkId();
                return getUserCredits(clerkId);
        }
        public Boolean hasEnoughCredits(int requiredCredits){
                UserCredits userCredits=getUserCredits();
                return userCredits.getCredits()>= requiredCredits;
        }
        public UserCredits consumeCredits(){
                UserCredits userCredits=getUserCredits();
                if(userCredits.getCredits()<=0){
                        return null;
                }
                userCredits.setCredits(userCredits.getCredits()-1);
                return userCreditsRepository.save(userCredits);
        }

       
}
