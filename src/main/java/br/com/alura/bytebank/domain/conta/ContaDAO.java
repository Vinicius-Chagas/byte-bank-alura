package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.domain.cliente.Cliente;
import br.com.alura.bytebank.domain.cliente.DadosCadastroCliente;
import com.mysql.cj.x.protobuf.MysqlxPrepare;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class ContaDAO {

    private Connection connection;
    ContaDAO(Connection connection){
        this.connection = connection;
    }

    public void salvar(DadosAberturaConta dadosDaConta){
        var cliente = new Cliente(dadosDaConta.dadosCliente());
        var conta = new Conta(dadosDaConta.numero(), cliente, BigDecimal.ZERO, true);
        String sql = "INSERT INTO conta (numero, saldo, cliente_nome, cliente_cpf, cliente_email, esta_ativa)" +
                "VALUES(?,?,?,?,?,?)";

        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, conta.getNumero());
            statement.setBigDecimal(2, BigDecimal.ZERO);
            statement.setString(3, dadosDaConta.dadosCliente().nome());
            statement.setString(4,dadosDaConta.dadosCliente().cpf());
            statement.setString(5,dadosDaConta.dadosCliente().email());
            statement.setBoolean(6,conta.getEstaAtiva());

            statement.execute();
            statement.close();
            connection.close();
        } catch(
                SQLException e){
            throw new RuntimeException(e);
        }

    }

    public Set<Conta> listar(){
        Set<Conta> contas = new HashSet<>();
        PreparedStatement statement = null;
        ResultSet resultSet;
        String sql = "SELECT * FROM conta WHERE esta_ativa = true";
        try {
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            while(resultSet.next()){
                Integer numero = resultSet.getInt(1);
                BigDecimal saldo = resultSet.getBigDecimal(2);
                String nome = resultSet.getString(3);
                String cpf = resultSet.getString(4);
                String email = resultSet.getString(5);
                Boolean estaAtivo = resultSet.getBoolean(6);
                DadosCadastroCliente dadosCliente = new DadosCadastroCliente(nome,cpf, email);
                Cliente cliente = new Cliente(dadosCliente);
                contas.add(new Conta(numero, cliente, saldo, true));
            }

            resultSet.close();
            statement.close();
            connection.close();

        } catch(SQLException e){
            throw new RuntimeException(e);
        }

        return contas;
    }

    public Conta listarPorNumero(Integer num){
        Conta conta = null;
        PreparedStatement statement = null;
        ResultSet resultSet;
        String sql = "SELECT * FROM conta WHERE numero = ? and esta_ativa = true";
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1,num);
            resultSet = statement.executeQuery();

            while(resultSet.next()){
                Integer numero = resultSet.getInt(1);
                BigDecimal saldo = resultSet.getBigDecimal(2);
                String nome = resultSet.getString(3);
                String cpf = resultSet.getString(4);
                String email = resultSet.getString(5);
                Boolean estaAtiva = resultSet.getBoolean(6);
                DadosCadastroCliente dadosCliente = new DadosCadastroCliente(nome,cpf, email);
                Cliente cliente = new Cliente(dadosCliente);
                conta = new Conta(numero, cliente, saldo, estaAtiva);
            }

            resultSet.close();
            statement.close();
            connection.close();

        } catch(SQLException e){
            throw new RuntimeException(e);
        }

        return conta;
    }

    public void alterar(int conta, BigDecimal valor)
    {
        String sql = "UPDATE conta SET saldo = saldo + ? WHERE numero = ?";
        try {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setBigDecimal(1,valor);
            statement.setInt(2,conta);
            statement.execute();
            statement.close();
            connection.commit();
            connection.close();
        } catch(SQLException e){
            try {
                connection.rollback();
            } catch (SQLException ex){
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    public void deletar(Integer num){
        String sql = "DELETE FROM conta WHERE numero = ?";
        try{
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1,num);

            statement.execute();
            statement.close();
            connection.close();
        } catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    public void alterarLogica(Integer num){
        String sql = "UPDATE conta SET esta_ativa = false WHERE numero = ?";
        try {
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1,num);
            statement.execute();
            statement.close();
            connection.commit();
            connection.close();
        } catch(SQLException e){
            try {
                connection.rollback();
            } catch (SQLException ex){
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }
}
