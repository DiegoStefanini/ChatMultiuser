package client;

import data.Packet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.google.gson.Gson;

public class AttendiRichieste implements Runnable {
    private static BufferedReader RiceviDalServer;
    private static PrintWriter MandaAlServer;
    private static Gson gson = new Gson();
    private static String inChat = "";
    private static ArrayList<Integer> NuoviMessaggi = new ArrayList<>();
    private static boolean InAttesa = true;
    public AttendiRichieste(BufferedReader ricevi, PrintWriter manda) {
        RiceviDalServer = ricevi;
        MandaAlServer = manda;
    }
    public static void cleenup() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    public static String[] getMenu(String newMessage) {
        try {
            InAttesa = false;
            Packet DaInviareAlServer = new Packet("CHAT", "", "", "", false);
            String json = gson.toJson(DaInviareAlServer); // converto il pacchetto in json
            MandaAlServer.println(json);
            json = RiceviDalServer.readLine();
            if (json == null) {
                throw new IOException("Connessione terminata dal server.");
            }
            Packet packetRicevuto = gson.fromJson(json, Packet.class);
            String[] chats = packetRicevuto.getContenuto().split(",\\s*");
            System.out.println("0 - Termina il programma");
            System.out.println("1 - Cerca utente o crea gruppo");

            if (chats.length > 0) {
                // Inizializzare la lista NuoviMessaggi per allinearla alla lunghezza di chats
                while (NuoviMessaggi.size() < chats.length) {
                    NuoviMessaggi.add(0); // Aggiunge contatori iniziali pari a 0
                }

                for (int j = 0; j < chats.length; j++) {
                    if (newMessage.equals(chats[j])) {
                        NuoviMessaggi.set(j, NuoviMessaggi.get(j) + 1);
                    }

                    String messaggiNonLetti = (NuoviMessaggi.get(j) > 0)
                            ? "[" + NuoviMessaggi.get(j) + "]"
                            : "";
                    System.out.println((j + 2) + " - " + chats[j] + " " + messaggiNonLetti);
                }
            }
            InAttesa = true;

            return chats;
        } catch (IOException e) {
            System.err.println("Errore durante la ricezione del menu: " + e.getMessage());
            return new String[0];
        }
    }

    public static void setChat(String chi) {
        inChat = chi;
    }
    public void run() {

        String json;
        Packet PacketRicevuto;
        while (true) {
            try {
                if (InAttesa) {
                    json = RiceviDalServer.readLine();
                    PacketRicevuto = gson.fromJson(json, Packet.class);
                    if ("MESSAGGIO".equals(PacketRicevuto.getHeader())) {
                        if (inChat.equals(PacketRicevuto.getDestinatario())) {
                            System.out.println(PacketRicevuto.getMittente() + ": " + PacketRicevuto.getContenuto());
                        } else if (inChat.equals("")) {
                            cleenup();
                            getMenu(PacketRicevuto.getMittente());
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
