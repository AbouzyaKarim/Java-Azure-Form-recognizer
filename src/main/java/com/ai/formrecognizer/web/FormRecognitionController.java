package com.ai.formrecognizer.web;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ai.formrecognizer.entities.Invoice;
import com.ai.formrecognizer.entities.InvoiceRecognitionResult;
import com.ai.formrecognizer.entities.Receipt;
import com.ai.formrecognizer.entities.RecognitionResult;
import com.ai.formrecognizer.repositories.InvoiceRecognitionResultsRepository;
import com.ai.formrecognizer.repositories.RecognitionResultsRepository;
import com.azure.ai.formrecognizer.FormRecognizerClient;
import com.azure.ai.formrecognizer.FormRecognizerClientBuilder;
import com.azure.ai.formrecognizer.models.FieldValueType;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Slf4j
public class FormRecognitionController {

    @Autowired
    private RecognitionResultsRepository resultsRepository;

    @Autowired
    private InvoiceRecognitionResultsRepository invoiceRecognitionResultsRepository;

    @Value("${azure.form.recognizer.key}")
    private String key;

    @Value("${azure.form.recognizer.endpoint}")
    private String endpoint;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/upload")
    public String upload() {
        return "upload";
    }
    
    @GetMapping("/results")
    public ModelAndView results() {
        List<RecognitionResult> recognitionResults = resultsRepository.findAll();
        
        ModelAndView modelAndView = new ModelAndView("results");
        modelAndView.addObject("recognitionResults", recognitionResults);

        return modelAndView;
    }

    ////
    @GetMapping("/resultsInvoices")
    public ModelAndView resultsInvoices() {
        List<InvoiceRecognitionResult> recognitionResults = invoiceRecognitionResultsRepository.findAll();

        ModelAndView modelAndView = new ModelAndView("resultsInvoices.html");
        modelAndView.addObject("recognitionResults", recognitionResults);

        return modelAndView;
    }

    /////
    
    /*@PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {        
        // Create FormRecognizerClient
        FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential(key))
            .endpoint(endpoint)
            .buildClient();

        try (InputStream receiptImage = file.getInputStream()) {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller = 
                formRecognizerClient.beginRecognizeReceipts(receiptImage, file.getSize());
            
            List<RecognizedForm> recognizedForms = syncPoller.getFinalResult();
            
            // Check if we have at least one form
            if(recognizedForms.size() >= 1) {                
                // Get recognized form
                final RecognizedForm recognizedForm = recognizedForms.get(0);
                                
                // Extract fields
                RecognitionResult recognitionResult = ExtractFormReceiptFields(file, recognizedForm);

                // Store result
                resultsRepository.save(recognitionResult);

                // Debut results
                System.out.println("\n\n--== Recognition result ==--\n\n" + recognitionResult.toString());                
            }
            
        } catch (IOException e) { 
            e.printStackTrace();
        }

        return "index";        
    }*/

    /////
    @GetMapping("/uploadInvoice")
    public String uploadInvoice() {
        return "uploadInvoice";
    }

    @PostMapping("/uploadInvoice")
    public String handleFileInvoiceUpload(@RequestParam("file") MultipartFile file) {
        // Create FormRecognizerClient
        FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
                .credential(new AzureKeyCredential(key))
                .endpoint(endpoint)
                .buildClient();

        try (InputStream invoiceImage = file.getInputStream()) {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                    formRecognizerClient.beginRecognizeInvoices(invoiceImage, file.getSize());

            List<RecognizedForm> recognizedForms = syncPoller.getFinalResult();

            // Check if we have at least one form
            if(recognizedForms.size() >= 1) {
                // Get recognized form
                final RecognizedForm recognizedForm = recognizedForms.get(0);

                // Extract fields
                InvoiceRecognitionResult recognitionResult = ExtractFormInvoiceFields(file, recognizedForm);

                // Store result
                invoiceRecognitionResultsRepository.save(recognitionResult);

                // Debut results
                System.out.println("\n\n--== Recognition result ==--\n\n" + recognitionResult.toString());

                ///
                System.out.println("************************All Feilds*************************");
                // Extract field values from the recognized form
                for (FormField field : recognizedForm.getFields().values()) {
                    // Check the confidence score of the field
                    if (field.getConfidence() >= 0.1) {
                        // Extract the field value based on its type
                        if (field.getValue().getValueType() == FieldValueType.STRING) {
                            String value = field.getValue().asString();
                            System.out.println(field.getName() + ": " + value);
                        } else if (field.getValue().getValueType() == FieldValueType.FLOAT) {
                            float value = field.getValue().asFloat();
                            System.out.println(field.getName() + ": " + value);
                        } else if (field.getValue().getValueType() == FieldValueType.DATE) {
                            LocalDate value = field.getValue().asDate();
                            System.out.println(field.getName() + ": " + value);
                        }
                        // Add more conditions for different value types if needed
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "index";
    }

    private InvoiceRecognitionResult ExtractFormInvoiceFields(MultipartFile file, RecognizedForm recognizedForm) {
        InvoiceRecognitionResult recognitionResult = new InvoiceRecognitionResult();

        Invoice invoice = new Invoice(recognizedForm);

        // Set receipt file name based on the upload image name
        recognitionResult.setReceiptFileName(file.getOriginalFilename());

        // Get Merchant name and transaction date
        //recognitionResult.setMerchantName(receipt.getReceiptType().getType());
        //recognitionResult.setTransactionDate(receipt.getTransactionDate().getValue());

        // Retrieve total
        Map<String, FormField> recognizedFields = recognizedForm.getFields();
        FormField InvoiceIdField = recognizedFields.get("InvoiceId");
        FormField customerNameField = recognizedFields.get("CustomerName");
        FormField invoiceDateField = recognizedFields.get("InvoiceDate");

        if (InvoiceIdField != null) {
            if (FieldValueType.STRING == InvoiceIdField.getValue().getValueType()) {
                recognitionResult.setInvoiceId(InvoiceIdField.getValue().asString());
            }
        }

        if (invoiceDateField != null) {

                recognitionResult.setInvoiceDate(invoiceDateField.getValue().asDate());
            }else{
                recognitionResult.setInvoiceDate(null);
            }


        if (customerNameField != null) {
            if (FieldValueType.STRING == customerNameField.getValue().getValueType()) {
                recognitionResult.setCustomerName(customerNameField.getValue().asString());
            }
        }
        return recognitionResult;
    }

    /*private RecognitionResult ExtractFormReceiptFields(MultipartFile file, final RecognizedForm recognizedForm) {
        RecognitionResult recognitionResult = new RecognitionResult();

        Receipt receipt = new Receipt(recognizedForm);
        
        // Set receipt file name based on the upload image name
        recognitionResult.setReceiptFileName(file.getOriginalFilename());

        // Get Merchant name and transaction date
        recognitionResult.setMerchantName(receipt.getReceiptType().getType());
        recognitionResult.setTransactionDate(receipt.getTransactionDate().getValue());                                
        
        // Retrieve total                
        Map<String, FormField> recognizedFields = recognizedForm.getFields();
        FormField totalField = recognizedFields.get("Total");
        if (totalField != null) {
            if (FieldValueType.FLOAT == totalField.getValue().getValueType()) {
                recognitionResult.setTotal(totalField.getValue().asFloat());
            }
        }
        return recognitionResult;
    }    */

    @GetMapping("/chart")
    public String chart() {
        return "chart";
    }

    @RequestMapping(value = "/getChartData", method = RequestMethod.GET) 
    @ResponseBody
    public String getChartData() throws JsonProcessingException {        
        ObjectMapper objectMapper = new ObjectMapper();
        
        List<Double> items = resultsRepository.findAll().stream().map(
            r -> r.getTotal()).collect(Collectors.toList());        

        return objectMapper.writeValueAsString(items);
    }
}