package com.progetto.timebank.bean;

public class BookedLessonBean {
    private String subject;
    private String tutorName;
    private String date;
    private String status;
    private final boolean tutorOnline;

    private int duration;
    private int transactionId;
    private int partnerId;

    public BookedLessonBean(String subject, String tutorName, String date, String status, boolean tutorOnline) {
        this.subject = subject;
        this.tutorName = tutorName;
        this.date = date;
        this.status = status;
        this.tutorOnline = tutorOnline;
    }

    public String getSubject() { return subject; }
    public String getTutorName() { return tutorName; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
    public boolean isTutorOnline() { return tutorOnline; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getPartnerId() { return partnerId; }
    public void setPartnerId(int partnerId) { this.partnerId = partnerId; }
}