package com.ai.formrecognizer.repositories;
import com.ai.formrecognizer.entities.RecognitionResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecognitionResultsRepository extends JpaRepository <RecognitionResult, Long> {
    
}
