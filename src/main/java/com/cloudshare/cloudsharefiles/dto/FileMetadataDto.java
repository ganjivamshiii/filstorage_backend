package com.cloudshare.cloudsharefiles.dto;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileMetadataDto {
    
     private String id;
     private String name;
     private String type;
     private Long  size;
     private String clerkId;
     private boolean isPublic;
     private String fileLocation;
     private LocalDateTime uploadedAt;

}
