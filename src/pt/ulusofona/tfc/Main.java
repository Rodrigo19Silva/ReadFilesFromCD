package pt.ulusofona.tfc;

import pt.ulusofona.tfc.filters.NumericFilter;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    public static ArrayList<String> leExemplos(File pasta) {
        ArrayList<String> conteudoFicheiros = new ArrayList<>();

        if (pasta == null || !pasta.exists() || !pasta.isDirectory()) {
            System.out.println("A pasta " + pasta + " não existe");
            return conteudoFicheiros;
        }

        String[] objetos = pasta.list();
        int i = 0;

        while (objetos != null && i < objetos.length) {
            File ficheiro = new File(pasta, objetos[i]);

            if (ficheiro.isFile() && ficheiro.getName().toLowerCase().endsWith(".txt")) {
                try (BufferedReader br = new BufferedReader(new FileReader(ficheiro))) {
                    StringBuilder conteudoDoFicheiro = new StringBuilder();
                    String linha;

                    while ((linha = br.readLine()) != null) {
                        conteudoDoFicheiro.append(linha).append("\n");
                    }

                    conteudoFicheiros.add(conteudoDoFicheiro.toString());

                } catch (IOException e) {
                    throw new RuntimeException("Erro ao ler o ficheiro: " + ficheiro.getAbsolutePath(), e);
                }
            }
            i++;
        }

        return conteudoFicheiros;
    }


    public static String enviarPedido(ArrayList<String> partes, String model) throws Exception {
        String apiKey = "";

        // Construir o array de mensagens
        StringBuilder messages = new StringBuilder();
        messages.append("{\"role\":\"system\",\"content\":\"You are a helpful assistant.\"}");

        if (partes != null) {
            int i = 0;
            while (i < partes.size()) {
                String conteudoFicheiro = partes.get(i);
                messages.append(",")
                        .append("{\"role\":\"user\",\"content\":\"")
                        .append(conteudoFicheiro)
                        .append("\"}");
                i++;
            }
        }

        // JSON do pedido
        String requestBody = "{"
                + "\"model\":\"" + model + "\","
                + "\"messages\":[" + messages + "],"
                + "\"temperature\":0.7"
                + "}";

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey) // vai vazio
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        // mostra o status na consola e devolve o corpo
        System.out.println("HTTP Status: " + response.statusCode());

        return response.body();
    }

    // modelos
    static String[] obterModelos() {
        return new String[]{"gpt-4o-mini", "gpt-4o", "gpt-4.1-mini"};
    }

    static void processarPedido(String folder, String modelo, int nrVersoes, JTextArea infoOut) {
        File dir = new File(folder);
        if (!dir.exists() || !dir.isDirectory()) {
            JOptionPane.showMessageDialog(null,
                    "A pasta indicada não existe ou não é um diretório:\n" + dir.getAbsolutePath(),
                    "Pasta inválida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ArrayList<String> exemplos = leExemplos(dir);

        if (exemplos.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "A pasta não contém ficheiros .txt para enviar.",
                    "Sem exemplos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        infoOut.append("Ficheiros lidos: " + exemplos.size() + "\n");
        infoOut.append("Modelo: " + modelo + " | Versões: " + nrVersoes + "\n");

        for (int v = 1; v <= nrVersoes; v++) {
            infoOut.append("A enviar versão " + v + "...\n");
            try {
                String jsonResposta = enviarPedido(exemplos, modelo);
                System.out.println(jsonResposta);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Erro na versão " + v + ":\n" + ex.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        infoOut.append("Concluído.\n");
    }

    // Grid layout
    public static void mostrarGUI() {
        JFrame window = new JFrame("TFC do Rodrigo");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //painel em estilo windows
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // pasta + o botão para selecionar as pastas do windows
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        painel.add(new JLabel("Pasta:"), gbc);

        JTextField pastaField = new JTextField();
        pastaField.setEditable(false);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        painel.add(pastaField, gbc);

        JButton selecionarPastaButton = new JButton("Selecionar Pasta");
        selecionarPastaButton.addActionListener(e -> {
            JFileChooser seletor = new JFileChooser();
            seletor.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int resultado = seletor.showOpenDialog(window);
            if (resultado == JFileChooser.APPROVE_OPTION) {
                File pasta = seletor.getSelectedFile();
                pastaField.setText(pasta.getAbsolutePath());
                infoTextArea.append("Pasta selecionada: " + pasta.getAbsolutePath() + "\n");
            }
        });
        gbc.gridx = 2; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        painel.add(selecionarPastaButton, gbc);

        // model do llm
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        painel.add(new JLabel("Modelo:"), gbc);

        JComboBox<String> modeloComboBox = new JComboBox<>(obterModelos());
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        painel.add(modeloComboBox, gbc);
        gbc.gridwidth = 1;

        // versões editavel
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        painel.add(new JLabel("Versões:"), gbc);

        Integer[] options = {1,2,3,4,5,6,7,8,9,10};
        JComboBox<Integer> versoesComboBox = new JComboBox<>(options);
        versoesComboBox.setEditable(true);
        JTextField editor = (JTextField) versoesComboBox.getEditor().getEditorComponent();
        ((AbstractDocument) editor.getDocument()).setDocumentFilter(new NumericFilter());
        editor.setText("1");

        gbc.gridx = 1; gbc.weightx = 0.3; gbc.fill = GridBagConstraints.HORIZONTAL;
        painel.add(versoesComboBox, gbc);

        gbc.gridx = 2; gbc.weightx = 0.7;
        painel.add(Box.createHorizontalStrut(1), gbc);

        // textarea one aparece as informações
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.NORTHWEST;
        painel.add(new JLabel("Informações:"), gbc);

        infoTextArea = new JTextArea(12, 60);
        infoTextArea.setEditable(false);
        infoTextArea.setLineWrap(true);
        infoTextArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(infoTextArea);

        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        painel.add(scrollPane, gbc);

        gbc.gridwidth = 1; gbc.weighty = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL;

        // botão submeter que fica com eventos
        JButton enviarButton = new JButton("Submeter");

        enviarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pasta = pastaField.getText().trim();
                if (pasta.isEmpty()) {
                    JOptionPane.showMessageDialog(window, "Seleciona uma pasta primeiro.",
                            "Pasta em falta", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String modelo = (String) modeloComboBox.getSelectedItem();
                if (modelo == null || modelo.isBlank()) {
                    JOptionPane.showMessageDialog(window, "Seleciona um modelo.",
                            "Modelo em falta", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Object sel = versoesComboBox.getSelectedItem();
                int nrVersoes;

                if (sel instanceof Integer) {
                    nrVersoes = (Integer) sel;
                } else {
                    assert sel != null;
                    String txt = sel.toString();
                    if (txt.isEmpty()) {
                        JOptionPane.showMessageDialog(window, "Indique o número de versões",
                                "Valor em falta", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    nrVersoes = Integer.parseInt(txt);
                }

                if (nrVersoes < 1) {
                    JOptionPane.showMessageDialog(window, "O número de versões tem de ser maior ou igual que 1.",
                            "Valor inválido", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                infoTextArea.append("A enviar dados...\n");
                infoTextArea.append("Pasta: " + pasta + "\n");
                infoTextArea.append("Modelo: " + modelo + "\n");

                processarPedido(pasta, modelo, nrVersoes, infoTextArea);
            }
        });



        gbc.gridx = 2; gbc.gridy = 4; gbc.anchor = GridBagConstraints.SOUTHEAST;
        painel.add(enviarButton, gbc);

        window.setContentPane(painel);
        window.pack();
        window.setMinimumSize(new Dimension(800, 500));
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    // variavel que guarda texto de informação
    private static JTextArea infoTextArea;


    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::mostrarGUI);
    }
}
