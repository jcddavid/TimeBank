package com.progetto.timebank.bean;

public class RequestBean {
    private int transactionId;
    private String studentName;
    private String subject;
    private String status;
    private boolean studentOnline;
    private int duration;
    private int partnerId;

    public RequestBean(int transactionId, String studentName, String subject, String status, boolean studentOnline) {
        this.transactionId = transactionId;
        this.studentName = studentName;
        this.subject = subject;
        this.status = status;
        this.studentOnline = studentOnline;
    }

    public int getTransactionId() { return transactionId; }
    public String getStudentName() { return studentName; }
    public String getSubject() { return subject; }
    public String getStatus() { return status; }

    public boolean isStudentOnline() { return studentOnline; }

    public int getPartnerId() { return partnerId; }
    public void setPartnerId(int partnerId) { this.partnerId = partnerId; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
}