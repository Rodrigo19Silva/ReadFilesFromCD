package pt.ulusofona.tfc;

import pt.ulusofona.tfc.filters.NumericFilter;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class Main {

    // lê todos os ficheiros .txt de uma pasta e devolve o conteúdo numa lista
    public static ArrayList<String> leExemplos(File pasta) {
        ArrayList<String> conteudoFicheiros = new ArrayList<>();

        // verificar se a pasta existe ou é diretório
        if (pasta == null || !pasta.exists() || !pasta.isDirectory()) {
            System.out.println("A pasta " + pasta + " não existe");
            return conteudoFicheiros;
        }

        // guardar nomes dos ficheiros
        String[] objetos = pasta.list();
        int i = 0;

        while (objetos != null && i < objetos.length) {
            File ficheiro = new File(pasta, objetos[i]);

            // só ficheiros normais e apenas se forem .txt
            if (ficheiro.isFile() && ficheiro.getName().toLowerCase().endsWith(".txt")) {
                try (BufferedReader br = new BufferedReader(new FileReader(ficheiro))) {
                    StringBuilder conteudoDoFicheiro = new StringBuilder();
                    String linha;

                    // ler o ficheiro linha a linha
                    while ((linha = br.readLine()) != null) {
                        conteudoDoFicheiro.append(linha).append("\n");
                    }

                    // adicionar o conteúdo à lista
                    conteudoFicheiros.add(conteudoDoFicheiro.toString());

                } catch (IOException e) {
                    throw new RuntimeException("Erro ao ler o ficheiro: " + ficheiro.getAbsolutePath(), e);
                }
            }
            i++;
        }

        return conteudoFicheiros;
    }

    public static String enviarPedido(ArrayList<String> partes) throws Exception {
        String apiKey = "";
        if (apiKey == null) {
            throw new IllegalStateException("Defina a variável de ambiente OPENAI_API_KEY.");
        }

        // construir o array de mensagens
        StringBuilder messages = new StringBuilder();
        messages.append("{\"role\":\"system\",\"content\":\"You are a helpful assistant.\"}");

        // adiciona cada parte como uma mensagem do utilizador
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

        // body do json
        String requestBody = "{"
                + "\"model\":\"gpt-4o-mini\","   // tipo de modelo
                + "\"messages\":[" + messages + "]," // mensagens
                + "\"temperature\":0.7"          // controla aleatoriedade da resposta
                + "}";

        // criar o cliente
        HttpClient client = HttpClient.newHttpClient();

        // cria o pedido
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // envia o pedido e recebe a resposta
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        // devolve o JSON
        return response.body();
    }

    static String[] obterModelos() {
        return new String[]{"GPT"};
    }

    static void processarPedido(String folder, String modelo, int nrVersoes) {
        ArrayList exemplos = leExemplos(new File(folder));

        try {
            String jsonResposta = enviarPedido(exemplos);

            System.out.println("Resposta do servidor:\n" + jsonResposta);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String mostrarGUI() {
        JFrame window = new JFrame();


        window.setSize(1600, 1000);
        window.setLocationRelativeTo(null);

        JPanel pane = new JPanel();

        window.add(pane);

        pane.setLayout(new GridLayout(4,2));

        // Input da Pasta
        pane.add(new JLabel("Folder"));
        JTextField folderTextField = new JTextField("");
        folderTextField.setSize(20, 20);
        folderTextField.setColumns(15);
        // no futuro vai ser 1 "browse folder"
        folderTextField.setText("C:\\Users\\ADMIN\\IdeaProjects\\ReadFilesFromCD\\teste-files");
        pane.add(folderTextField);

        // Input do Modelo
        pane.add(new JLabel("Model"));
        String[] modelos = obterModelos();
        JComboBox<String> dropdown = new JComboBox<>(modelos);
        pane.add(dropdown);

        // Input do nr de versoes
        pane.add(new JLabel("Versões"));
        Integer[] options = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        JComboBox<Integer> numberBox = new JComboBox<>(options);
        numberBox.setEditable(true);
        JTextField editor = (JTextField) numberBox.getEditor().getEditorComponent();
        ((AbstractDocument) editor.getDocument()).setDocumentFilter(new NumericFilter());
        pane.add(numberBox);



        JButton enviarButton = new JButton("Enviar");
        enviarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //JOptionPane.showMessageDialog(window, "Você clicou no botão!");

                String pasta = folderTextField.getText();


                // - ir buscar o nr versões
                int nrVersoes = (int) numberBox.getSelectedItem();
                // - ir buscar o nome do modelo
                String modelo = (String) dropdown.getSelectedItem();
                processarPedido(pasta, modelo, nrVersoes);

            }
        });

        pane.add(enviarButton);

        window.setVisible(true);

        return null;
    }


    public static void main(String[] args) throws Exception {

        mostrarGUI();

        //workflowDemo();

    }

    public static void workflowDemo() throws Exception {
        File pasta = new File("teste-files");

        ArrayList exemplos = leExemplos(pasta);

        System.out.println("Ficheiros lidos: " + exemplos.size());

        String jsonResposta = enviarPedido(exemplos);

        System.out.println("Resposta do servidor:\n" + jsonResposta);
    }
}
