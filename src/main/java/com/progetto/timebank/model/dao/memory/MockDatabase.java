package com.progetto.timebank.model.dao.memory;

import com.progetto.timebank.model.Course;
import com.progetto.timebank.model.User;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MockDatabase {

    private MockDatabase() {
        throw new IllegalStateException("Utility class");
    }

    // ACCETTABILE PER LA DEMO: E' UN DATABASE FINTO (Viene usato come struct)
    @SuppressWarnings("java:S2386")
    public static final List<User> USERS = new ArrayList<>();

    @SuppressWarnings("java:S2386")
    public static final List<TransactionRecord> TRANSACTIONS = new ArrayList<>();

    private static int userIdCounter = 1;
    private static int transIdCounter = 1;

    // Classe interna per simulare la riga di una transazione
    @SuppressWarnings("java:S1104") // Ignora warning sui campi pubblici (è una struct)
    public static class TransactionRecord {
        public int id;
        public int studentId;
        public int tutorId;
        public String subject;
        public String status;
        public Timestamp date;
        public Timestamp startTime;
        public int duration;
        public int rating;

        public TransactionRecord(int id, int sId, int tId, String sub) {
            this.id = id;
            this.studentId = sId;
            this.tutorId = tId;
            this.subject = sub;
            this.status = "PENDING";
            this.date = new Timestamp(System.currentTimeMillis());
        }
    }

    // Inizializzazione dati finti
    static {
        // Utente 1
        User u1 = new User();
        u1.setId(userIdCounter++);
        u1.setUsername("David");
        u1.setPassword("mitomito132");
        u1.setEmail("david@email.com");
        u1.setBalance(3); // Do un po' di saldo per testare
        u1.setSkills(Course.ANALISI_MATEMATICA_I.toString());
        u1.setAverageRating(4.5);
        u1.setOnline(true);

        // Utente 2
        User u2 = new User();
        u2.setId(userIdCounter++);
        u2.setUsername("Omar");
        u2.setPassword("mitomito132");
        u2.setEmail("omar@email.com");
        u2.setBalance(5);
        u2.setSkills(Course.CALCOLATORI_ELETTRONICI.toString());
        u2.setAverageRating(3.0);
        u2.setOnline(true);

        USERS.add(u1);
        USERS.add(u2);
    }

    public static int getNextUserId() { return userIdCounter++; }
    public static int getNextTransId() { return transIdCounter++; }
}