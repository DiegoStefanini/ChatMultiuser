package client;

import data.Packet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.google.gson.Gson;

public class AttendiRichieste implements Runnable {
    private BufferedReader riceviDalServer;
    private PrintWriter mandaAlServer;
    private static Gson gson = new Gson();
    private volatile String inChat = "";
    private ArrayList<Integer> nuoviMessaggi = new ArrayList<>();
    private volatile boolean inAttesa = false;
    private volatile boolean running = true;

    public AttendiRichieste(BufferedReader ricevi, PrintWriter manda) {
        this.riceviDalServer = ricevi;
        this.mandaAlServer = manda;
    }

    public void stop() {
        running = false;
    }

    public static void cleenup() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    public String[] getMenu(String newMessage) {
        try {
            inAttesa = false;
            Packet daInviareAlServer = new Packet("CHAT", "", "", "", false);
            String json = gson.toJson(daInviareAlServer); // converto il pacchetto in JSON
            mandaAlServer.println(json);
            json = riceviDalServer.readLine();

            if (json == null) {
                throw new IOException("Connessione terminata dal server.");
            }

            Packet packetRicevuto = gson.fromJson(json, Packet.class);
            String[] chats = packetRicevuto.getContenuto().split(",\\s*");
            System.out.println("0 - Termina il programma");
            System.out.println("1 - Cerca utente o crea gruppo");

            // Sincronizza la lista nuoviMessaggi con chats
            while (nuoviMessaggi.size() < chats.length) {
                nuoviMessaggi.add(0);
            }
            if (nuoviMessaggi.size() > chats.length) {
                nuoviMessaggi = new ArrayList<>(nuoviMessaggi.subList(0, chats.length));
            }

            for (int j = 0; j < chats.length; j++) {
                if (newMessage.equals(chats[j])) {
                    nuoviMessaggi.set(j, nuoviMessaggi.get(j) + 1);
                }

                String messaggiNonLetti = (nuoviMessaggi.get(j) > 0)
                        ? "[" + nuoviMessaggi.get(j) + "]"
                        : "";
                System.out.println((j + 2) + " - " + chats[j] + " " + messaggiNonLetti);
            }

            inAttesa = true;
            return chats;
        } catch (IOException e) {
            System.err.println("Errore durante la ricezione del menu: " + e.getMessage());
            return new String[0];
        }
    }

    public void setChat(String chi) {
        inChat = chi;
    }

    @Override
    public void run() {
        String json;
        Packet packetRicevuto;

        while (running) {
            try {
                if (inAttesa) {
                    json = riceviDalServer.readLine();

                    if (json == null) {
                        System.err.println("Connessione interrotta dal server.");
                        break;
                    }

                    try {
                        packetRicevuto = gson.fromJson(json, Packet.class);
                    } catch (Exception e) {
                        System.err.println("Errore nel parsing del JSON: " + e.getMessage());
                        continue;
                    }

                    if ("MESSAGGIO".equals(packetRicevuto.getHeader())) {
                        if (inChat.equals(packetRicevuto.getMittente())) {
                            System.out.println(packetRicevuto.getMittente() + ": " + packetRicevuto.getContenuto());
                        } else if (inChat.equals("")) {
                            cleenup();
                            getMenu(packetRicevuto.getMittente());
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Errore di I/O: " + e.getMessage());
                break;
            } catch (Exception e) {
                System.err.println("Errore generico: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
