package com.cloudshare.cloudsharefiles.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.cloudshare.cloudsharefiles.document.FileMetadataDocument;
import com.cloudshare.cloudsharefiles.document.ProfileDocument;
import com.cloudshare.cloudsharefiles.document.UserCredits;

public interface UserCreditsRepository extends MongoRepository<UserCredits, String> {
    Optional<UserCredits> findByClerkId(String clerkId);
}
