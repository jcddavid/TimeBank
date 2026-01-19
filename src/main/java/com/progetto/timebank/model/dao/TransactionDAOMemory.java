package com.progetto.timebank.model.dao;

import com.progetto.timebank.bean.BookedLessonBean;
import com.progetto.timebank.bean.RequestBean;
import com.progetto.timebank.model.dao.memory.MockDatabase;
import com.progetto.timebank.exception.DuplicateBookingException;
import com.progetto.timebank.exception.InsufficientBalanceException;
import com.progetto.timebank.model.User;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOMemory implements TransactionDAO {

    @Override
    public void bookLesson(int studentId, int tutorId, String subject) throws DuplicateBookingException, InsufficientBalanceException {
        // Controllo saldo
        User student = findUser(studentId);
        if (student == null || student.getBalance() <= 0) {
            throw new InsufficientBalanceException("Saldo insufficiente (Memory Check).");
        }

        // Controllo duplicati
        boolean exists = MockDatabase.TRANSACTIONS.stream()
                .anyMatch(t -> t.studentId == studentId && t.tutorId == tutorId
                        && t.subject.equals(subject) && "PENDING".equals(t.status));
        if (exists) throw new DuplicateBookingException("Prenotazione già esistente (Demo Memory).");

        // Creazione transazione
        MockDatabase.TransactionRecord t = new MockDatabase.TransactionRecord(
                MockDatabase.getNextTransId(), studentId, tutorId, subject);
        MockDatabase.TRANSACTIONS.add(t);
    }

    @Override
    public List<BookedLessonBean> getLessonsByStudentId(int studentId) {
        List<BookedLessonBean> list = new ArrayList<>();
        for (MockDatabase.TransactionRecord t : MockDatabase.TRANSACTIONS) {
            if (t.studentId == studentId) {
                User tutor = findUser(t.tutorId);
                String tutorName = (tutor != null) ? tutor.getUsername() : "Sconosciuto";
                // DEMO: Tutor sempre online
                boolean isOnline = true;

                BookedLessonBean bean = new BookedLessonBean(
                        t.subject, tutorName, t.date.toString(), t.status, isOnline);
                bean.setTransactionId(t.id);
                bean.setPartnerId(t.tutorId);
                bean.setDuration(t.duration);
                list.add(bean);
            }
        }
        return list;
    }

    @Override
    public List<RequestBean> getPendingRequestsByTutorId(int tutorId) {
        List<RequestBean> list = new ArrayList<>();
        for (MockDatabase.TransactionRecord t : MockDatabase.TRANSACTIONS) {
            boolean isTargetTutor = (t.tutorId == tutorId);
            boolean isValidStatus = "PENDING".equals(t.status) ||
                    "ACCEPTED".equals(t.status) ||
                    "IN_PROGRESS".equals(t.status);

            if (isTargetTutor && isValidStatus) {
                User student = findUser(t.studentId);
                String studName = (student != null) ? student.getUsername() : "Sconosciuto";
                // DEMO: Studente sempre online
                boolean isOnline = true;

                RequestBean bean = new RequestBean(t.id, studName, t.subject, t.status, isOnline);
                bean.setPartnerId(t.studentId);
                bean.setDuration(t.duration);
                list.add(bean);
            }
        }
        return list;
    }

    @Override
    public void updateTransactionStatus(int transactionId, String newStatus) {
        MockDatabase.TRANSACTIONS.stream()
                .filter(t -> t.id == transactionId)
                .findFirst()
                .ifPresent(t -> t.status = newStatus);
    }

    @Override
    public Timestamp startLessonIfNotStarted(int transId) {
        MockDatabase.TransactionRecord t = findTrans(transId);
        if (t != null) {
            if (t.startTime == null) {
                t.startTime = new Timestamp(System.currentTimeMillis());
                t.status = "IN_PROGRESS";
            }
            return t.startTime;
        }
        return null;
    }

    @Override
    public Timestamp getLessonStartTime(int transId) {
        MockDatabase.TransactionRecord t = findTrans(transId);
        return (t != null) ? t.startTime : null;
    }

    @Override
    public void finalizeLesson(int transId, int studentId, int tutorId, int duration) {
        MockDatabase.TransactionRecord t = findTrans(transId);
        if (t != null) {
            t.status = "COMPLETED";
            t.duration = duration;
            // Aggiorna saldo in memoria
            User student = findUser(studentId);
            User tutor = findUser(tutorId);
            if (student != null) student.setBalance(student.getBalance() - 1);
            if (tutor != null) tutor.setBalance(tutor.getBalance() + 1);
        }
    }

    @Override
    public void submitRating(int transactionId, int rating) {
        MockDatabase.TransactionRecord t = findTrans(transactionId);
        if (t != null) t.rating = rating;
    }

    // Helper privati
    private User findUser(int id) {
        return MockDatabase.USERS.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
    }

    private MockDatabase.TransactionRecord findTrans(int id) {
        return MockDatabase.TRANSACTIONS.stream().filter(t -> t.id == id).findFirst().orElse(null);
    }
}