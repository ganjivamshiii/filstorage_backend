package com.cloudshare.cloudsharefiles.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudshare.cloudsharefiles.service.UserCreditsService;

import lombok.RequiredArgsConstructor;
import com.cloudshare.cloudsharefiles.document.UserCredits;
import com.cloudshare.cloudsharefiles.dto.UserCreditsDto;
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserCreditsController {

    private final UserCreditsService userCreditsService;
    @GetMapping("/credits")
    public ResponseEntity<?> getUserCredits(){
        System.out.println("user enter the request");
        UserCredits userCredits=userCreditsService.getUserCredits();
        UserCreditsDto response=UserCreditsDto.builder()
                  .credits(userCredits.getCredits())
                  .plan(userCredits.getPlan())
                  .build();
        return ResponseEntity.ok(response);
    }
}
