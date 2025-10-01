package pt.ulusofona.tfc;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class Main {

    // lê todos os ficheiros .txt de uma pasta e devolve o conteúdo numa lista
    public static ArrayList<String> leExemplos(File pasta) {
        ArrayList<String> conteudoFicheiros = new ArrayList<>();

        // verificar se a pasta existe e é diretório
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

    public static void main(String[] args) throws Exception {
        File pasta = new File("testes-files");
        ArrayList<String> exemplos = leExemplos(pasta);
        System.out.println("Ficheiros lidos: " + exemplos.size());

        String jsonResposta = enviarPedido(exemplos);


        System.out.println("Resposta do servidor:\n" + jsonResposta);
    }
}
