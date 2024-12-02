package server;
import server.ServerMain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler implements Runnable {
    //attributi
    Socket link;
    GestoreClients gestore;
    int MioIndice;
    public ClientHandler(Socket s, GestoreClients v, int z) {
        this.link = s;
        this.gestore = v;
        this.MioIndice = z;
    }

    public void cleanup(int index) {
        try {
            link.close();
        } catch (IOException e) {
        }
        gestore.dec(index);
    }

    public static void attendi(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }


    public void run() {
        BufferedReader reader = null;
        PrintWriter writer = null;
        String message = null;
        String nome = null;
        String VuoleChattareCon = null;
        try {
            // RICEVE MESSAGGI DAL CLIENT
            reader = new BufferedReader(new InputStreamReader(link.getInputStream()));

            // INVIA MESSAGGI AL CLIENT
            writer = new PrintWriter(link.getOutputStream(), true);
        }catch(IOException ex) {
            ex.printStackTrace();
            System.out.println("Client died too early, cleaning up the mess!");
            cleanup(MioIndice);
            return;
        }
        try {
            nome = reader.readLine();
            gestore.impostaNome(nome, MioIndice);
            VuoleChattareCon = reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while(!"".equalsIgnoreCase(message)) {
            try {
                //legge il next message
                message = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            // Legge il messaggio dal client

/*
            /*Verifico se il messaggio è valido e rispondo solo in quel caso
                // Calcola un tempo casuale basato sul numero di client attivi
                int delay = gestore.get() * 200; // Millisecondi
                System.out.println("Tempo di attesa per rispondere: " + delay + " ms");

                // Attende per il tempo calcolato
                attendi(delay);

                // Invia la risposta al client
                writer.println("World");
                System.out.println("Risposta inviata al client: World");*/



        }
        try {
            reader.close();
            writer.close();
        } catch (IOException e) {
        }
        cleanup(MioIndice);

    }
}