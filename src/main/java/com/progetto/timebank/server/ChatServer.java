package com.progetto.timebank.server;

import com.progetto.timebank.config.AppConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    private static final int PORT = 12345;
    // Mappa thread-safe per gestire i client connessi
    private static final Map<Socket, ClientInfo> clients = new ConcurrentHashMap<>();

    private static volatile boolean isRunning = true;
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        System.out.println("TimeBank Chat Server in avvio su porta " + PORT + "...");
        if (AppConfig.getIsDemo()) {
            System.out.println("[MODALITÀ DEMO ATTIVA]: Le lezioni partiranno immediatamente con un solo utente.");
        }

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server pronto e in ascolto.");

            while (isRunning) {
                acceptConnection();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void acceptConnection() {
        try {
            Socket clientSocket = serverSocket.accept();
            new Thread(new ClientHandler(clientSocket)).start();
        } catch (IOException e) {
            if (isRunning) {
                e.printStackTrace();
            } else {
                System.out.println("Server arrestato manualmente.");
            }
        }
    }

    // Metodo per fermare il server
    public static void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Classe interna per memorizzare info del client
    private static class ClientInfo {
        String roomId;
        String username;

        public ClientInfo(String roomId, String username) {
            this.roomId = roomId;
            this.username = username;
        }
    }

    // Thread per gestire la singola connessione
    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println("[Ricevuto]: " + message);
                    String[] parts = message.split(":");

                    // PROTOCOLLO: JOIN:transId:username
                    if (parts[0].equals("JOIN") && parts.length == 3) {
                        handleJoin(parts);
                    } else {
                        // Messaggio normale di chat
                        ClientInfo senderInfo = clients.get(socket);
                        if (senderInfo != null) {
                            broadcastToRoom(senderInfo.username + ": " + message, senderInfo.roomId, socket);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Connessione interrotta con un client.");
            } finally {
                removeClient();
            }
        }

        private void handleJoin(String[] parts) {
            String roomId = parts[1];
            String username = parts[2];

            ClientInfo newInfo = new ClientInfo(roomId, username);
            clients.put(socket, newInfo);

            System.out.println("[LOG] " + username + " è entrato nella stanza " + roomId);
            broadcastToRoom("SYSTEM:" + username + " è entrato.", roomId, socket);

            // --- MODIFICA PER VERSIONE DEMO ---
            if (AppConfig.getIsDemo()) {
                System.out.println("[DEMO] Avvio forzato lezione stanza " + roomId);
                broadcastToRoom("CMD:START", roomId, null);
                broadcastToRoom("SYSTEM:[DEMO] Lezione avviata in modalità test.", roomId, null);
            }
            // --- LOGICA STANDARD (NON DEMO) ---
            else if (countUsersInRoom(roomId) >= 2) {
                // Se ci sono 2 utenti, invia il comando di start a tutti
                System.out.println("[LOG] Stanza " + roomId + " piena. Invio CMD:START");
                broadcastToRoom("CMD:START", roomId, null);
            }
        }

        private void removeClient() {
            ClientInfo info = clients.remove(socket);
            if (info != null) {
                System.out.println("[LOG] " + info.username + " è uscito (Stanza " + info.roomId + ")");
                // Notifica uscita a tutti gli altri nella stanza
                broadcastToRoom("SYSTEM:" + info.username + " ha lasciato la chat.", info.roomId, null);
            }
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        // Metodo per inviare messaggi a una specifica stanza
        private static void broadcastToRoom(String msg, String roomId, Socket excludeSocket) {
            clients.forEach((clientSock, info) -> {
                if (info.roomId.equals(roomId) && clientSock != excludeSocket) {
                    try {
                        PrintWriter clientOut = new PrintWriter(clientSock.getOutputStream(), true);
                        clientOut.println(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        private static int countUsersInRoom(String roomId) {
            long count = clients.values().stream()
                    .filter(info -> info.roomId.equals(roomId))
                    .count();
            return (int) count;
        }
    }
}