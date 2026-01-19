package com.progetto.timebank.model.dao;

import com.progetto.timebank.bean.BookedLessonBean;
import com.progetto.timebank.bean.RequestBean;
import com.progetto.timebank.exception.DuplicateBookingException;
import com.progetto.timebank.exception.PersistenceException;
import com.progetto.timebank.model.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOCSV implements TransactionDAO {

    private static final String CSV_FILE = "transactions.csv";

    @Override
    public void bookLesson(int studentId, int tutorId, String subject)
            throws PersistenceException, DuplicateBookingException {
        try {
            if (checkDuplicateCSV(studentId, tutorId, subject)) {
                throw new DuplicateBookingException("Richiesta già presente (CSV Check).");
            }

            int newId = getNextId();
            String date = new Timestamp(System.currentTimeMillis()).toString();
            String startTime = "null";
            int rating = 0;

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_FILE, true))) {
                String line = newId + "," + studentId + "," + tutorId + "," + subject + ",PENDING," + date + "," + startTime + "," + rating;
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            throw new PersistenceException("Errore Scrittura su File CSV.", e);
        }
    }

    @Override
    public List<BookedLessonBean> getLessonsByStudentId(int studentId) throws PersistenceException {
        List<BookedLessonBean> list = new ArrayList<>();
        File f = new File(CSV_FILE);
        if (!f.exists()) return list;

        UserDAO userDAO = DAOFactory.getUserDAO();

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                BookedLessonBean bean = createLessonFromLine(parts, studentId, userDAO);
                if (bean != null) {
                    list.add(bean);
                }
            }
            return list;
        } catch (IOException e) {
            throw new PersistenceException("Errore Lettura File CSV.", e);
        }
    }

    private BookedLessonBean createLessonFromLine(String[] parts, int studentId, UserDAO userDAO) {
        if (parts.length >= 6 && Integer.parseInt(parts[1]) == studentId) {
            int tutorId = Integer.parseInt(parts[2]);
            String tutorName = "TutorID:" + tutorId;
            boolean isOnline = false;
            try {
                User u = userDAO.getUserById(tutorId);
                if (u != null) tutorName = u.getUsername();
                isOnline = userDAO.isUserOnline(tutorId);
            } catch (Exception e) {
                // ignore
            }
            BookedLessonBean b = new BookedLessonBean(
                    parts[3], tutorName, parts[5], parts[4], isOnline
            );
            b.setTransactionId(Integer.parseInt(parts[0]));
            b.setPartnerId(tutorId);
            b.setDuration(1);
            return b;
        }
        return null;
    }

    @Override
    public List<RequestBean> getPendingRequestsByTutorId(int tutorId) throws PersistenceException {
        List<RequestBean> list = new ArrayList<>();
        File f = new File(CSV_FILE);
        if (!f.exists()) return list;

        UserDAO userDAO = DAOFactory.getUserDAO();

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                RequestBean req = createRequestFromLine(parts, tutorId, userDAO);
                if (req != null) {
                    list.add(req);
                }
            }
            return list;
        } catch (IOException e) {
            throw new PersistenceException("Errore Lettura File CSV.", e);
        }
    }

    private RequestBean createRequestFromLine(String[] parts, int tutorId, UserDAO userDAO) throws PersistenceException {
        if (parts.length >= 6 && Integer.parseInt(parts[2]) == tutorId) {
            String status = parts[4];
            if ("PENDING".equals(status) || "ACCEPTED".equals(status) || "IN_PROGRESS".equals(status)) {
                int studentId = Integer.parseInt(parts[1]);
                String studentName = "StudenteID:" + studentId;
                boolean isOnline = false;

                try {
                    User u = userDAO.getUserById(studentId);
                    if (u != null) studentName = u.getUsername();
                    isOnline = userDAO.isUserOnline(studentId);
                } catch (SQLException e) {
                    throw new PersistenceException("Error in the database");
                }

                RequestBean req = new RequestBean(
                        Integer.parseInt(parts[0]), studentName, parts[3], status, isOnline
                );
                req.setPartnerId(studentId);
                req.setDuration(1);
                return req;
            }
        }
        return null;
    }

    @Override
    public void updateTransactionStatus(int transactionId, String newStatus) throws PersistenceException {
        try {
            File f = new File(CSV_FILE);
            if (!f.exists()) return;
            List<String> lines = Files.readAllLines(Paths.get(CSV_FILE));
            List<String> newLines = new ArrayList<>();

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                newLines.add(processUpdateStatusLine(line, parts, transactionId, newStatus));
            }
            rewriteFile(newLines);
        } catch (IOException e) {
            throw new PersistenceException("Errore Aggiornamento CSV.", e);
        }
    }

    private String processUpdateStatusLine(String line, String[] parts, int transactionId, String newStatus) {
        if (Integer.parseInt(parts[0]) == transactionId) {
            String startTime = (parts.length > 6) ? parts[6] : "null";
            String rating = (parts.length > 7) ? parts[7] : "0";
            return parts[0] + "," + parts[1] + "," + parts[2] + "," + parts[3] + ","
                    + newStatus + "," + parts[5] + "," + startTime + "," + rating;
        }
        return line;
    }

    @Override
    public Timestamp startLessonIfNotStarted(int transId) throws PersistenceException {
        // Estrazione del controllo file per ridurre nesting
        File f = new File(CSV_FILE);
        if (!f.exists()) return new Timestamp(System.currentTimeMillis());

        try {
            List<String> lines = Files.readAllLines(Paths.get(CSV_FILE));
            List<String> newLines = new ArrayList<>();

            Timestamp[] returnTime = { new Timestamp(System.currentTimeMillis()) };
            boolean[] updated = { false };

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (Integer.parseInt(parts[0]) == transId) {
                    processStartLogic(line, parts, returnTime, newLines, updated);
                } else {
                    newLines.add(line);
                }
            }

            if (updated[0]) rewriteFile(newLines);
            return returnTime[0];
        } catch (IOException e) {
            throw new PersistenceException("Errore IO Start Lesson.", e);
        }
    }

    private void processStartLogic(String line, String[] parts, Timestamp[] returnTime, List<String> newLines, boolean[] updated) {
        String existingStart = (parts.length > 6) ? parts[6] : "null";

        if (!"null".equals(existingStart)) {
            returnTime[0] = Timestamp.valueOf(existingStart);
            newLines.add(line);
        } else {
            returnTime[0] = new Timestamp(System.currentTimeMillis());
            newLines.add(createInProgressLine(parts, returnTime[0]));
            updated[0] = true;
        }
    }

    private String createInProgressLine(String[] parts, Timestamp returnTime) {
        String rating = (parts.length > 7) ? parts[7] : "0";
        return parts[0] + "," + parts[1] + "," + parts[2] + "," + parts[3] + ","
                + "IN_PROGRESS" + "," + parts[5] + "," + returnTime.toString() + "," + rating;
    }

    @Override
    public Timestamp getLessonStartTime(int transId) throws PersistenceException {
        try {
            File f = new File(CSV_FILE);
            if (!f.exists()) return null;
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(",");
                    if (Integer.parseInt(parts[0]) == transId) {
                        if (parts.length > 6 && !"null".equals(parts[6])) return Timestamp.valueOf(parts[6]);
                        return null;
                    }
                }
            }
            return null;
        } catch (IOException e) {
            throw new PersistenceException("Errore IO Lettura StartTime.", e);
        }
    }

    @Override
    public void finalizeLesson(int transId, int studentId, int tutorId, int hoursCost) throws PersistenceException {
        updateTransactionStatus(transId, "COMPLETED");
        updateUserBalanceSQL(studentId, -hoursCost);
        updateUserBalanceSQL(tutorId, hoursCost);
    }

    private void updateUserBalanceSQL(int userId, int amount) {
        try {
            java.sql.Connection conn = DBConnection.getInstance();
            String sql = "UPDATE Users SET balance = balance + ? WHERE user_id = ?";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, amount);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void submitRating(int transactionId, int rating) throws PersistenceException {
        try {
            List<String> lines = Files.readAllLines(Paths.get(CSV_FILE));
            List<String> newLines = new ArrayList<>();
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                newLines.add(processRatingLine(line, parts, transactionId, rating));
            }
            rewriteFile(newLines);
        } catch (IOException e) {
            throw new PersistenceException("Errore Scrittura Rating.", e);
        }
    }

    private String processRatingLine(String line, String[] parts, int transactionId, int rating) {
        if (Integer.parseInt(parts[0]) == transactionId) {
            String startTime = (parts.length > 6) ? parts[6] : "null";
            return parts[0] + "," + parts[1] + "," + parts[2] + "," + parts[3] + ","
                    + parts[4] + "," + parts[5] + "," + startTime + "," + rating;
        }
        return line;
    }

    private void rewriteFile(List<String> lines) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_FILE, false))) {
            for (String l : lines) { bw.write(l); bw.newLine(); }
        }
    }

    private int getNextId() throws IOException {
        File f = new File(CSV_FILE);
        if (!f.exists()) return 1;
        int maxId = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length > 0) {
                    try {
                        int currentId = Integer.parseInt(parts[0]);
                        if (currentId > maxId) maxId = currentId;
                    } catch (NumberFormatException e) {e.printStackTrace();}
                }
            }
        }
        return maxId + 1;
    }

    private boolean checkDuplicateCSV(int sId, int tId, String subj) throws IOException {
        File f = new File(CSV_FILE);
        if (!f.exists()) return false;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (isDuplicate(line, sId, tId, subj)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isDuplicate(String line, int sId, int tId, String subj) {
        String[] p = line.split(",");
        return p.length >= 5 && Integer.parseInt(p[1]) == sId && Integer.parseInt(p[2]) == tId &&
                p[3].equals(subj) && p[4].equals("PENDING");
    }
}