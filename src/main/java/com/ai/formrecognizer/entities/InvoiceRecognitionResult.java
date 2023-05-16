package com.ai.formrecognizer.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "invoiceRecognitionResults")
public class InvoiceRecognitionResult {
    @javax.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "receiptFileName")
    private String receiptFileName;

    @Column(name = "customerName")
    private String customerName;

    @Column(name = "invoiceId")
    private String invoiceId;

    @Column(name = "invoiceDate")
    private LocalDate invoiceDate;

    public String toString() {
        return String.format("Customer name: %s\nInvoice ID: %s\nInvoice Date:%.2f",
                getCustomerName(), getInvoiceId(), getInvoiceDate());
    }
}
