package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;


public class ServerMain {

    public static final int CLIENT_DELAY = 200;



    public static void main(String[] args) {
        int port = 12345; // Porta su cui il server ascolta
        GestoreClients gestore = new GestoreClients();
        int index;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server avviato. In attesa di connessioni...");

            while (true) {
                // Accetta e aspetta la connessione di un client una nuova connessione dal client
                Socket client = serverSocket.accept();
                System.out.println("Nuovo client connesso.");
                index = gestore.inc(client);
                Thread handler = new Thread(new ClientHandler(client,gestore, index));
                handler.start();
            }

        } catch (Exception e) {
            System.err.println("Errore del server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}