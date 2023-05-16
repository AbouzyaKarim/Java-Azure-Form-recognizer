package com.ai.formrecognizer.entities;

import com.azure.ai.formrecognizer.models.FieldData;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.annotation.Immutable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public final class Invoice {

    /**
     * Recognized field merchant name.
     */
    private TypedFormField<String> customerName;

    /**
     * Recognized field invoice ID.
     */
    private TypedFormField<String> invoiceId;

    /**
     * Recognized field invoice Date.
     */
    private TypedFormField<LocalDate> invoiceDate;




    /**
     * Constructs a invoice object from the provided recognized form.
     * @param recognizedForm the recognized form object.
     */
    public Invoice(RecognizedForm recognizedForm) {
        for (Map.Entry<String, FormField> entry : recognizedForm.getFields().entrySet()) {
            String key = entry.getKey();
            FormField formField = entry.getValue();
            switch (key) {

                case "CustomerName":
                    customerName = new TypedFormField<>(formField, String.class);
                    break;
                case "InvoiceId":
                    invoiceId = new TypedFormField<>(formField, String.class);
                    break;
                case "InvoiceDate":
                    invoiceDate = new TypedFormField<>(formField, LocalDate.class);
                    break;

                default:
                    break;
            }
        }
    }


    /**
     * The strongly typed FormField representation model.
     *
     * @param <T> The type of value returned from the service call.
     */
    public static class TypedFormField<T> {
        private final FormField formField;
        private final Class<T> type;

        /**
         * Constructs a TypedFormField object.
         *
         * @param formField the SDK returned FormField object.
         * @param type      The type of the field value returned from the service call.
         */
        public TypedFormField(FormField formField, Class<T> type) {
            this.formField = formField;
            this.type = type;
        }

        /**
         * Get the strongly typed value of the recognized field.
         *
         * @return the strongly typed value of the recognized field.
         * @throws IllegalStateException when a type mismatch occurs.
         */
        @SuppressWarnings("unchecked")
        public T getValue() {
            switch (formField.getValue().getValueType()) {
                case STRING:
                    if (type.isAssignableFrom(String.class)) {
                        return (T) formField.getValue().asString();
                    } else {
                        throw new IllegalStateException("Type mismatch error occurred.");
                    }
                case DATE:
                    if (type.isAssignableFrom(LocalDate.class)) {
                        return (T) formField.getValue().asDate();
                    } else {
                        throw new IllegalStateException("Type mismatch error occurred.");
                    }
                case TIME:
                    if (type.isAssignableFrom(LocalTime.class)) {
                        return (T) formField.getValue().asTime();
                    } else {
                        throw new IllegalStateException("Type mismatch error occurred.");
                    }
                case PHONE_NUMBER:
                    if (type.isAssignableFrom(String.class)) {
                        return (T) formField.getValue().asPhoneNumber();
                    } else {
                        throw new IllegalStateException("Type mismatch error occurred.");
                    }
                case FLOAT:
                    if (type.isAssignableFrom(Double.class)) {
                        return (T) formField.getValue().asFloat();
                    } else {
                        throw new IllegalStateException("Type mismatch error occurred.");
                    }
                case LONG:
                    if (type.isAssignableFrom(Long.class)) {
                        return (T) formField.getValue().asLong();
                    } else {
                        throw new IllegalStateException("Type mismatch error occurred.");
                    }
                case LIST:
                    if (type.isAssignableFrom(List.class)) {
                        return (T) formField.getValue().asList();
                    } else {
                        throw new IllegalStateException("Type mismatch error occurred.");
                    }
                case MAP:
                    if (type.isAssignableFrom(Map.class)) {
                        return (T) formField.getValue().asMap();
                    } else {
                        throw new IllegalStateException("Type mismatch error occurred.");
                    }
                default:
                    throw new IllegalStateException("Unexpected type value: " + formField.getValue().getValueType());
            }
        }
    }
}
