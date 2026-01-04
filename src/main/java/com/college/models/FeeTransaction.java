package com.college.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FeeTransaction {
    public enum Type {
        PAYMENT, REFUND, WAIVER, FINE
    }

    public enum PaymentMode {
        CASH, ONLINE, CHEQUE, TRANSFER
    }

    private int id;
    private String transactionId;
    private int studentId;
    private Integer feePaymentId; // Can be null
    private BigDecimal amount;
    private Type type;
    private String description;
    private PaymentMode paymentMode;
    private LocalDateTime transactionDate;
    private int createdBy;

    public FeeTransaction() {
    }

    public FeeTransaction(String transactionId, int studentId, BigDecimal amount, Type type, PaymentMode mode) {
        this.transactionId = transactionId;
        this.studentId = studentId;
        this.amount = amount;
        this.type = type;
        this.paymentMode = mode;
        this.transactionDate = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public Integer getFeePaymentId() {
        return feePaymentId;
    }

    public void setFeePaymentId(Integer feePaymentId) {
        this.feePaymentId = feePaymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
}
