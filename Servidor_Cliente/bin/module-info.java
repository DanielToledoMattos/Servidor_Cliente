module Servidor_Cliente {
    requires java.desktop;       // Libera javax.swing, AWT, etc.
    requires java.net.http;      // Se usar API HTTP moderna
    // requires java.logging;   // (opcional) Se for usar logs
    requires java.base; // Módulo padrão do Java 
    requires flatlaf;
}