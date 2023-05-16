package com.ai.formrecognizer.repositories;

import com.ai.formrecognizer.entities.InvoiceRecognitionResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRecognitionResultsRepository extends JpaRepository<InvoiceRecognitionResult, Long> {
}
