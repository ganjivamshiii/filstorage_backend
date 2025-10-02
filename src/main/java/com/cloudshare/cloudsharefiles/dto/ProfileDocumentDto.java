package com.cloudshare.cloudsharefiles.dto;

import java.time.Instant;

import org.springframework.data.annotation.Id;

import com.cloudshare.cloudsharefiles.document.ProfileDocument.ProfileDocumentBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Builder   // <-- This enables builder()
@Data
public class ProfileDocumentDto {
    @Id
    private String id;
    private String clerkId;
    private String email;
    private String firstname;
    private String lastname;
    private Integer credits;
    private String photoUrl;
    private Instant createdAt;
}
