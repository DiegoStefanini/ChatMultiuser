package data;

import java.util.ArrayList;

public class Packet {
    private String header; // LOGIN, LOGUOT; REGISTRA; SENDMESSAGGIO; CREAGRUPPO; NOTIFICATION;
    // avevo fatto così per poter mandare un unico pacchetto al server
    // ma è scomodo, quindi mandiamo piu pacchetti al server per i gruppi
    // ArrayList<String> destinatario;
    private String destinatario;
    private String mittente;
    private String contenuto;
    private boolean errore;
    public Packet(String h, String d, String m, String c, boolean e) {
        this.header = h;
        this.destinatario = d;
        this.mittente = m;
        this.contenuto = c;
        this.errore = e;
    }
    public boolean getError() {return errore; }
    public void setErrore(boolean come) {
        this.errore = come;
    }
    public String getHeader() { return header; }
    public void setType(String header) { this.header = header; }

    public String getMittente() { return mittente; }
    public void setMittente(String sender) { this.mittente = sender; }

    public String getDestinatario() {
        return destinatario;
    }
    public void addDestinatario(String dest) {
        destinatario = dest;
    }

    public String getContenuto() { return contenuto; }
    public void setContenuto(String content) { this.contenuto = content; }
}