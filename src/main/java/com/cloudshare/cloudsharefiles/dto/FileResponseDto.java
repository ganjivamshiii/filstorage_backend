package com.cloudshare.cloudsharefiles.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileResponseDto {
    private String fileName;
    private String downloadUrl;
    private String message;

}
