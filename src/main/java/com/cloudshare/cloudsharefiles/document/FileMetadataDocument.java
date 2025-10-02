package com.cloudshare.cloudsharefiles.document;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "files")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FileMetadataDocument{

     @Id
     private String id;
     private String name;
     private String type;
     private Long  size;
     private String clerkId;
     private Boolean isPublic;
     private String fileLocation;
     private LocalDateTime uploadedAt;


}