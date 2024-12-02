package client;

import com.sun.source.tree.BindingPatternTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientMain {

    private static final int MAX_TRY = 3;

    public static void attendi(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

    public static void main(String[] args) {
        // Configura l'indirizzo IP e la porta del server
        String serverAddress = "127.0.01"; // IP del server (localhost)
        int port = 12345; // Porta del server
        String input;
        int selezione;
        Socket link = null;
        int i = 0;

        while( link == null && i < MAX_TRY ) {
            try {
                // Crea una connessione al server
                link = new Socket(serverAddress, port);
            }catch(IOException ex) {
                System.out.println("Connessione non ruscita, tentativo "+(i+1)+ "/3");

                attendi((long) (1000*Math.pow(2, i)));
            }
            i++;
        }

        if( link == null ) {
            System.out.println("Impossibile collegarsi al server");
            return;
        }

        try {

            // Ottiene l'output stream per inviare dati
            OutputStream out = link.getOutputStream();

            // Ottiene l'output stream per ricevere dati
            InputStream in = link.getInputStream();

            // Usa un PrintWriter per inviare RIGHE di testo al server
            PrintWriter writer = new PrintWriter(out, true);
            // Usa un BufferReader per leggere RIGHE di testo al server
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            Scanner scanner = new Scanner(System.in);
            System.out.println("Inserisci il tuo nome: ");
            input = scanner.nextLine();
            // invio il nome al server
            writer.println(input);
            Thread attendi = new Thread(new AttendiRichieste());
            attendi.start();
            System.out.println("Premi 1 per chiedere il collegamento a qualcuno");
            System.out.println("Premi 2 per vedere chi è online");
            selezione = scanner.nextInt();


            if (selezione == 1) {
                System.out.print("Inserisci il nome con cui vuoi chattare: ");
                input = scanner.nextLine();
                // invio il nome con cui voglio chattare al server
                writer.println(input);
                writer.println();
                System.out.println("Attendi che l'altro accetti la connessione");
            } else if (selezione == 2) {
                System.out.println("Work in progress...");
            }
        } catch (Exception e) {
            // Gestisce eventuali errori
            System.err.println("Errore: " + e.getMessage());
            e.printStackTrace();
        }
    }

}