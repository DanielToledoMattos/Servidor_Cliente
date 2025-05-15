package privateChatServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServidorGUI extends JFrame {
    private static final int PORTA = 12345;

    private Map<String, String> usuariosValidos = Map.of(
        "Daniel", "Daniel123",
        "Kayky", "Kayky123",
        "Gustavo", "Gustavo123"
    );

    private JTabbedPane tabbedPane;
    private JTextArea logArea;
    private Map<Socket, PrintWriter> clienteWriters = new HashMap<>();
    private Map<Socket, JTextArea> clienteTextAreas = new HashMap<>();

    public ChatServidorGUI() {
        setTitle("Servidor - Central de Atendimento");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        logArea = new JTextArea(5, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        JScrollPane logScrollPane = new JScrollPane(logArea);
        add(logScrollPane, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        iniciarServidor();
    }

    private void iniciarServidor() {
        Thread serverThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
                appendLog("Servidor aguardando conexões na porta " + PORTA + "...");

                while (true) {
                    Socket clienteSocket = serverSocket.accept();
                    PrintWriter out = new PrintWriter(clienteSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));

                    String nomeCliente = in.readLine();
                    String senhaCliente = in.readLine();

                    if (!usuariosValidos.containsKey(nomeCliente) || !usuariosValidos.get(nomeCliente).equals(senhaCliente)) {
                        out.println("Acesso negado: nome de usuário ou senha incorretos.");
                        clienteSocket.close();
                        continue;
                    }

                    out.println("AUTENTICADO");
                    final String finalNomeCliente = nomeCliente;
                    clienteWriters.put(clienteSocket, out);

                    JTextArea chatArea = new JTextArea();
                    chatArea.setEditable(false);
                    chatArea.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
                    chatArea.setBackground(new Color(30, 30, 30));
                    chatArea.setForeground(Color.WHITE);
                    JScrollPane scrollPane = new JScrollPane(chatArea);

                    JTextField inputField = new JTextField();
                    inputField.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
                    inputField.setBackground(new Color(45, 45, 45));
                    inputField.setForeground(Color.WHITE);

                    JButton sendButton = new JButton("Enviar");
                    sendButton.setBackground(new Color(34, 140, 255));
                    sendButton.setForeground(Color.WHITE);

                    JButton emojiButton = new JButton("😀");
                    emojiButton.setBackground(new Color(34, 140, 255));
                    emojiButton.setForeground(Color.WHITE);

                    JPanel panel = new JPanel(new BorderLayout());
                    panel.add(scrollPane, BorderLayout.CENTER);

                    JPanel inputPanel = new JPanel(new BorderLayout());
                    inputPanel.add(emojiButton, BorderLayout.WEST);
                    inputPanel.add(inputField, BorderLayout.CENTER);
                    inputPanel.add(sendButton, BorderLayout.EAST);
                    panel.add(inputPanel, BorderLayout.SOUTH);

                    clienteTextAreas.put(clienteSocket, chatArea);

                    SwingUtilities.invokeLater(() -> {
                        appendLog("Novo cliente autenticado: " + finalNomeCliente);
                        tabbedPane.addTab(finalNomeCliente, panel);
                    });

                    sendButton.addActionListener(e -> {
                        String msg = inputField.getText();
                        if (!msg.isEmpty()) {
                            out.println("Servidor: " + msg);
                            chatArea.append("Servidor: " + msg + "\n");
                            inputField.setText("");
                        }
                    });

                    emojiButton.addActionListener(e -> showEmojiPanelFor(inputField, this));

                    inputField.addActionListener(e -> sendButton.doClick());

                    Thread clientThread = new Thread(() -> {
                        String line;
                        try {
                            while ((line = in.readLine()) != null) {
                                String finalLine = line;
                                SwingUtilities.invokeLater(() -> {
                                    chatArea.append(finalNomeCliente + ": " + finalLine + "\n");
                                });
                            }
                        } catch (IOException e) {
                            appendLog("Cliente desconectado: " + finalNomeCliente);
                        } finally {
                            try {
                                clienteSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            clienteWriters.remove(clienteSocket);
                            clienteTextAreas.remove(clienteSocket);
                        }
                    });

                    clientThread.start();
                }
            } catch (IOException e) {
                appendLog("Erro no servidor: " + e.getMessage());
            }
        });

        serverThread.start();
    }

    private void showEmojiPanelFor(JTextField inputField, JFrame parentFrame) {
        JDialog emojiDialog = new JDialog(parentFrame, "Escolha um Emoji", true);
        emojiDialog.setLayout(new GridLayout(2, 5, 5, 5));

        String[] emojis = {"😊", "😂", "❤️", "😎", "😢", "😡", "👍", "👎", "🤔", "🥺"};

        for (String emoji : emojis) {
            JLabel emojiLabel = new JLabel(emoji, SwingConstants.CENTER);
            emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
            emojiLabel.setOpaque(true);
            emojiLabel.setBackground(Color.WHITE);
            emojiLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            emojiLabel.setPreferredSize(new Dimension(50, 50));
            emojiLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            emojiLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    inputField.setText(inputField.getText() + " " + emoji);
                    emojiDialog.dispose();
                }

                public void mouseEntered(MouseEvent e) {
                    emojiLabel.setBackground(new Color(220, 220, 220));
                }

                public void mouseExited(MouseEvent e) {
                    emojiLabel.setBackground(Color.WHITE);
                }
            });

            emojiDialog.add(emojiLabel);
        }

        emojiDialog.pack();
        emojiDialog.setLocationRelativeTo(parentFrame);
        emojiDialog.setVisible(true);
    }

    private void appendLog(String msg) {
        SwingUtilities.invokeLater(() -> logArea.append(msg + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatServidorGUI servidor = new ChatServidorGUI();
            servidor.setVisible(true);
        });
    }
}


