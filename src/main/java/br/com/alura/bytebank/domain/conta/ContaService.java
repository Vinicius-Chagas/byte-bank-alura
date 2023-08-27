package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.domain.RegraDeNegocioException;
import br.com.alura.bytebank.domain.cliente.Cliente;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ContaService {
    private ConnectionFactory connection;
    public ContaService(){
        this.connection = new ConnectionFactory();
    }

    private Set<Conta> contas = new HashSet<>();

    public Set<Conta> listarContasAbertas() {
        Connection connect = connection.connect();
        return new ContaDAO(connect).listar();
    }

    public BigDecimal consultarSaldo(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        Connection connect = connection.connect();
        return new ContaDAO(connect).listarPorNumero(numeroDaConta).getSaldo();
    }

    public void abrir(DadosAberturaConta dadosDaConta) {
        Connection connect = connection.connect();
        new ContaDAO(connect).salvar(dadosDaConta);
    }

    public void realizarSaque(Integer numeroDaConta, BigDecimal valor) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do saque deve ser superior a zero!");
        }

        if (valor.compareTo(conta.getSaldo()) > 0) {
            throw new RegraDeNegocioException("Saldo insuficiente!");
        }

        if(!conta.getEstaAtiva()){
            throw new RegraDeNegocioException("Conta inativa");
        }

        Connection connect = connection.connect();
        new ContaDAO(connect).alterar(conta.getNumero(),valor.negate());
    }

    public void realizarDeposito(Integer numeroDaConta, BigDecimal valor) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do deposito deve ser superior a zero!");
        }

        Connection connect = connection.connect();
        new ContaDAO(connect).alterar(conta.getNumero(),valor);

    }

    public void encerrar(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (conta.possuiSaldo()) {
            throw new RegraDeNegocioException("Conta não pode ser encerrada pois ainda possui saldo!");
        }

        Connection connect = connection.connect();
        new ContaDAO(connect).deletar(numeroDaConta);
    }

    public void encerrarLogica(Integer numeroDaConta){
        var conta = buscarContaPorNumero(numeroDaConta);
        if (conta.possuiSaldo()) {
            throw new RegraDeNegocioException("Conta não pode ser encerrada pois ainda possui saldo!");
        }

        Connection connect = connection.connect();
        new ContaDAO(connect).alterarLogica(numeroDaConta);
    }

    private Conta buscarContaPorNumero(Integer numero) {
        Connection connect = connection.connect();
        Conta conta = new ContaDAO(connect).listarPorNumero(numero);
        if(conta != null){
            return conta;
        } else{
            throw new RegraDeNegocioException("Não existe conta cadastrada com esse número.");
        }
    }

    public void realizarTransferencia(Integer contaOrigem, Integer contaDestino, BigDecimal valor){
        this.realizarSaque(contaOrigem,valor);
        this.realizarDeposito(contaDestino, valor);
    }
}
