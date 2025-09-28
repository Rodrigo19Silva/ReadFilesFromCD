import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static ArrayList<String> leExemplos (File pasta) {
        ArrayList<String> conteudoFicheiros = new ArrayList<>();

        if (pasta == null || !pasta.exists()){
            System.out.println("A pasta " + pasta + " não existe");
            return conteudoFicheiros;
        }

        String [] objetos = pasta.list();
        int i = 0;

        while (objetos != null && i < objetos.length){
            File ficheiro = new File(pasta, objetos[i]);

            if (ficheiro.isFile()){
                try (Scanner lerFicheiro = new Scanner(ficheiro)) {
                    StringBuilder linhaConteudoFicheiro = new StringBuilder();
                    while (lerFicheiro.hasNextLine()){
                        linhaConteudoFicheiro.append(lerFicheiro.nextLine()).append("\n");
                    }

                    conteudoFicheiros.add(linhaConteudoFicheiro.toString());

                } catch (FileNotFoundException e) {
                    throw new RuntimeException("Erro ao ler o ficheiro");
                }
            }
            i++;
        }


        return  conteudoFicheiros;
    }
    public static void main(String[] args) {
        File pasta = new File("C:/Users/admin/IdeaProjects/leExemplos");
        ArrayList<String> exemplos = leExemplos(pasta);

        int i = 0;
        while (i < exemplos.size()) {
            System.out.println("---- FICHEIRO ----");
            System.out.println(exemplos.get(i));
            i++;
        }
    }
}