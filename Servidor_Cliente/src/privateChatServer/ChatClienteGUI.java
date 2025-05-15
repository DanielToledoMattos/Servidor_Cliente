package privateChatServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClienteGUI extends JFrame {
    private JTextArea textArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton emojiButton;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ChatClienteGUI(String serverAddress, int port) {
        setTitle("Cliente - Inspetor Ambiental");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Configurando a área de chat
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(new Color(30, 30, 30));
        textArea.setForeground(Color.WHITE);
        textArea.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));  // Fonte para emojis
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Campo de entrada de texto
        inputField = new JTextField();
        inputField.setBackground(new Color(45, 45, 45));
        inputField.setForeground(Color.WHITE);
        inputField.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));  // Fonte para emojis

        // Botões de envio e emojis
        sendButton = new JButton("Enviar");
        sendButton.setBackground(new Color(34, 140, 255));
        sendButton.setForeground(Color.WHITE);

        emojiButton = new JButton("😀");
        emojiButton.setBackground(new Color(34, 140, 255));
        emojiButton.setForeground(Color.WHITE);

        // Painel de entrada
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(emojiButton, BorderLayout.WEST);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Adicionando os componentes ao layout
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        emojiButton.addActionListener(e -> showEmojiPanel());

        connectToServer(serverAddress, port);
        startReaderThread();
    }

    private void connectToServer(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Pede credenciais do usuário
            String nomeUsuario = JOptionPane.showInputDialog(this, "Digite seu nome de usuário:");
            String senha = JOptionPane.showInputDialog(this, "Digite sua senha:");

            // Envia as credenciais para o servidor
            out.println(nomeUsuario);
            out.println(senha);

            // Recebe a resposta de autenticação
            String resposta = in.readLine();
            if (!"AUTENTICADO".equals(resposta)) {
                appendText("Falha na autenticação. Encerrando conexão.");
                socket.close();
                sendButton.setEnabled(false);
                inputField.setEnabled(false);
                return;
            }

            appendText("Conectado ao servidor " + serverAddress + ":" + port);

        } catch (IOException e) {
            appendText("Erro ao conectar: " + e.getMessage());
            sendButton.setEnabled(false);
            inputField.setEnabled(false);
        }
    }

    private void startReaderThread() {
        Thread reader = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    appendText(line);
                }
            } catch (IOException e) {
                appendText("Conexão encerrada.");
            }
        });
        reader.start();
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            out.println(message);
            appendText("Você: " + message);
            inputField.setText("");
        }
    }

    private void appendText(String message) {
        SwingUtilities.invokeLater(() -> textArea.append(message + "\n"));
    }

    private void showEmojiPanel() {
        JDialog emojiDialog = new JDialog(this, "Escolha um Emoji", true);
        emojiDialog.setLayout(new GridLayout(2, 5, 5, 5)); // 2 linhas, 5 colunas, com espaçamento

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
                @Override
                public void mouseClicked(MouseEvent e) {
                    insertEmoji(emojiLabel.getText());
                    emojiDialog.dispose();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    emojiLabel.setBackground(new Color(220, 220, 220));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    emojiLabel.setBackground(Color.WHITE);
                }
            });

            emojiDialog.add(emojiLabel);
        }

        emojiDialog.pack();
        emojiDialog.setLocationRelativeTo(this);
        emojiDialog.setVisible(true);
    }



    private void insertEmoji(String emoji) {
        inputField.setText(inputField.getText() + " " + emoji);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Pede o domínio
            String serverIP = JOptionPane.showInputDialog(null, "Digite o domínio fornecido pelo Serveo (ex: serveo.net ou xxxxx.serveo.net):");
            if (serverIP == null || serverIP.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Nenhum domínio inserido. Encerrando o aplicativo.");
                return;
            }
            serverIP = serverIP.replaceFirst("https?://", "").trim();

            // Pede a porta
            String portaStr = JOptionPane.showInputDialog(null, "Digite a porta fornecida pelo Serveo (ex: 38757):");
            int porta;
            try {
                porta = Integer.parseInt(portaStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Porta inválida. Encerrando o aplicativo.");
                return;
            }

            ChatClienteGUI client = new ChatClienteGUI(serverIP, porta);
            client.setVisible(true);
        });
    }
}

