package br.com.alura.TabelaFipe.principal;

import br.com.alura.TabelaFipe.model.Dados;
import br.com.alura.TabelaFipe.model.Modelos;
import br.com.alura.TabelaFipe.model.Veiculo;
import br.com.alura.TabelaFipe.service.ConsumoApi;
import br.com.alura.TabelaFipe.service.ConverteDdados;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {

    private final Scanner leitura = new Scanner(System.in);
    private final ConsumoApi consumo = new ConsumoApi();
    private final ConverteDdados conversor = new ConverteDdados();
    private final String URL_BASE = "https://parallelum.com.br/fipe/api/v1/";

    public void exibeMenu() {
        var menu = """
            *** OPÇÕES ***
            Carro
            Moto
            Caminhão

            Digite uma das opções para consulta:
            """;

        System.out.println(menu);
        var opcao = leitura.nextLine().toLowerCase();
        String tipoVeiculo;

        if (opcao.contains("carr")) {
            tipoVeiculo = "carros";
        } else if (opcao.contains("mot")) {
            tipoVeiculo = "motos";
        } else {
            tipoVeiculo = "caminhoes";
        }

        // 1. Listar marcas
        String endereco = URL_BASE + tipoVeiculo + "/marcas";
        String json = consumo.obterDados(endereco);
        List<Dados> marcas = conversor.obterLista(json, Dados.class);

        marcas.stream()
                .sorted(Comparator.comparing(Dados::codigo))
                .forEach(m -> System.out.println("Código: " + m.codigo() + " | Nome: " + m.nome()));

        System.out.println("\nDigite o código da marca:");
        var codigoMarca = leitura.nextLine();

        // 2. Listar modelos da marca
        endereco = URL_BASE + tipoVeiculo + "/marcas/" + codigoMarca + "/modelos";
        json = consumo.obterDados(endereco);
        Modelos modeloLista = conversor.obterDados(json, Modelos.class);

        System.out.println("\nModelos dessa Marca:");
        modeloLista.modelos().stream()
                .sorted(Comparator.comparing(Dados::codigo))
                .forEach(m -> System.out.println("Código: " + m.codigo() + " | Nome: " + m.nome()));

        System.out.println("\nDigite um trecho do nome do modelo para filtrar:");
        var trechoModelo = leitura.nextLine();

        var modelosFiltrados = modeloLista.modelos().stream()
                .filter(m -> m.nome().toLowerCase().contains(trechoModelo.toLowerCase()))
                .collect(Collectors.toList());

        System.out.println("\nModelos encontrados:");
        modelosFiltrados.forEach(m -> System.out.println("Código: " + m.codigo() + " | Nome: " + m.nome()));

        System.out.println("\nDigite o código do modelo para ver os anos disponíveis:");
        var codigoModelo = leitura.nextLine();

        // 3. Listar anos disponíveis
        endereco = URL_BASE + tipoVeiculo + "/marcas/" + codigoMarca + "/modelos/" + codigoModelo + "/anos";
        json = consumo.obterDados(endereco);
        List<Dados> anos = conversor.obterLista(json, Dados.class);

        // 4. Buscar avaliação de cada ano
        List<Veiculo> veiculos = anos.stream()
                .map(d -> {
                    String enderecoAno = URL_BASE + tipoVeiculo + "/marcas/" + codigoMarca + "/modelos/" + codigoModelo + "/anos/" + d.codigo();
                    String jsonAno = consumo.obterDados(enderecoAno);
                    return conversor.obterDados(jsonAno, Veiculo.class);
                })
                .collect(Collectors.toList());

        System.out.println("\nTodos os veículos filtrados com avaliações por ano:");
        veiculos.forEach(System.out::println);
    }
}

