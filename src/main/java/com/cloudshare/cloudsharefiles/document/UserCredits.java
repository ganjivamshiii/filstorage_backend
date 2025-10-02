package com.cloudshare.cloudsharefiles.document;

 import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "user_credits")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCredits {
    @Id
    private String id;
    private String clerkId;
    private Integer credits;
    private String plan;
}

