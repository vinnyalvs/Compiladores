/* Feito por Vinicius Alberto Alves da Silva */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.HashMap;

public class BasicCalculator {

    static HashMap<String, Integer> variable_storage = new HashMap<String, Integer>();
    static Stack<String> stack_values = new Stack<>(); //  Pilha dos valores, sejam números ou variáveis
    static Stack<String> stack_operations = new Stack<>(); //Pilha dos operadores * e +

    public static void main(String[] args){

        //Chama ReadFile colocando o arquivo em memória
        //Depois processa cada linha do arquivo individualmente

        ArrayList <String> lines = readFile(args[0]);
        for (int i=0;i<lines.size();i++){
            if(!lines.get(i).isEmpty()) {
                String aux= lines.get(i);
                if(aux.charAt(aux.length()-1) == ';')
                    // Se ultimo caractere da linha não for ; da erro
                     processLine(lines.get(i));
                else{
                    breakAndPrintError("Erro: entrada inválida");
                }

            }
        }

    }

    //Le arquivo linha por linha e coloca em um ArrayList
     private static ArrayList <String> readFile(String filePath) {
            BufferedReader reader;
            ArrayList <String> lines  = new ArrayList<>() ;
            try {
               reader = new BufferedReader(new FileReader(filePath));
               String line = reader.readLine();
                while (line != null) {
                    lines.add(line);
                    line = reader.readLine();
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return lines;
    }

    private static void breakAndPrintError(String message){
        System.out.println(message);
        System.exit(1);
    }


    private static void processLine(String line){

        line = line.replace(" ","");
        line = line.replace("\t",""); //Remove espaços
        String[] symbolsToPutOnStack = line.split("(?==)|(?<==)|(?=\\+)|(?<=\\+)|(?=\\*)|(?<=\\*)|(?=;)");
        //Esse regex é fundamental para conseguir separar cada possível símbolo da linguagem em uma string diferente
        // symbolsToPutOnStack é o conteúdo da line dividido em tokens
        int i =0;
        String c;
        c = symbolsToPutOnStack[i]; // o primeiro simbolo de cada linha tem que ser uma variável
        if(c.equals(c.toUpperCase())) // checa se é lowercase
            breakAndPrintError("Erro: Não é variável válida");
        String key = c;
        i++;
        c = symbolsToPutOnStack[i]; // tem que ser = ou ; senão erro
        if(c.equals(";")){
                //CASO:  Comando de impressão na tela.
                if(variable_storage.containsKey(key))
                    System.out.println(variable_storage.get(key));
                else
                    breakAndPrintError("Erro: Variável não declarada");
        } else if(c.equals("=")){ //CASO:  Comando de atribuição.
                i++;
                c = symbolsToPutOnStack[i]; // proximo caractere tem que ser uma variavel ou numero 
                Boolean flag = isNumeric(c) || !c.equals(c.toUpperCase());
                if(flag) { // vamos atribuir o valor de key na variable_storage
                    stack_values.push(c);
                    i++;
                    for (; i < symbolsToPutOnStack.length ; i++) { // vai passar por todos os simbolos restantes
                        c = symbolsToPutOnStack[i];
                        if(c.equals(";")) { // chegou no final da linha hora de atribuir o valor na pilha a key variable_storage
                            int value = calculateStackValue();
                            stack_operations.clear();
                            stack_values.clear();
                            variable_storage.put(key, value);
                            break;
                        } else if(c.equals("+") || c.equals("*")){ // Se for uma operação, basta inserir seguindo a lógica descrito no readme
                            int aux = i + 1;
                            String nextElement = symbolsToPutOnStack[aux];
                            if(!(isNumeric(nextElement) || !nextElement.equals(nextElement.toUpperCase())))  //verifica se o próximo caracter é valido
                                breakAndPrintError("Erro: entrada inválida");

                            controlPushInsert(c); // Controla a precedência de operadores
                        } else if(isNumeric(c) || !c.equals(c.toUpperCase()) ){
                            if(!isNumeric(c)) // Ao inserir na stack_values checa se tokens já foram declarados
                                if(!variable_storage.containsKey(c))
                                    breakAndPrintError("Erro: Variável não declarada");
                            stack_values.push(c); // Insere na pilha para calcular em calculateStackValue();
                        } else
                            breakAndPrintError("Erro: entrada inválida");
                    }
                } else {
                    breakAndPrintError("Erro: entrada inválida");
                }
        } else {
            breakAndPrintError("Erro: entrada inválida");
        }


    }

    //Controla inserção na stack_values, recursiva
    //Caso base stack_values vazia
    //Segundo Caso base inserir * e ja tem um asterisco na stack_values de simbolos
    //recursão quando vai inserir + e * ja esta na stack_values

    private static void controlPushInsert(String c){
        if(stack_operations.empty())
            stack_operations.push(c);
        else {
            String last_symbol = stack_operations.peek(); // Espia o próximo simbolo
            if(last_symbol.equals("*") && c.equals("+")){
                String result = String.valueOf(getResult(last_symbol));
                String symbol = stack_operations.pop(); // descarta simbolo
                stack_values.push(result);
                controlPushInsert(c);
            } else
                stack_operations.push(c);
        }
    }


    //Serve para checar se uma string é um número
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            int d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    //Faz a operação com os dois últimos números da stack_values
    private static int makeOperation(String symbol) {
        int result;
        String number1 = stack_values.pop();
        // Se é um número deixa o valor dele mesmo, se for variavel pega o valor na variable_storage
        number1 = isNumeric(number1)  ? number1 : String.valueOf(variable_storage.get(number1));
        String number2 = stack_values.pop();
        number2 = isNumeric(number2)  ? number2 : String.valueOf(variable_storage.get(number2));
        if(symbol.equals("*"))
            result = Integer.parseInt(number1) * Integer.parseInt(number2);
        else
            result = Integer.parseInt(number1) + Integer.parseInt(number2);
        return result;
    }

    private static int getResult(String symbol) {
        return makeOperation(symbol);
    }

    private static int getResult() {
        String symbol = stack_operations.pop();
        return makeOperation(symbol);
    }

    //Calcula o resultado de toda a stack_values
    private static int calculateStackValue() {
        int result = 0;
        while (true){
            if(stack_operations.isEmpty()){ //Se não há mais operações para fazer basta retornar valor da pilha
                String aux = stack_values.pop();
                result = Integer.parseInt(aux);
                break;
            }
            if(stack_values.size() < 2) // Se < 2 então só a um valor na pilha não é preciso fazer mais operações
                break;
            int result_aux =  getResult();
            stack_values.push(String.valueOf(result_aux));
        }
        return result;
    }


}

