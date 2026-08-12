package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gerenciador de conexão JDBC com MySQL.
 * Obtém parâmetros via variáveis de ambiente com valores padrão para desenvolvimento.
 */
public class Conexao {

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    // Variáveis de ambiente configuráveis
    private static final String DB_HOST = getEnvOrDefault("DB_HOST", "localhost");
    private static final String DB_PORT = getEnvOrDefault("DB_PORT", "3306");
    private static final String DB_NAME = getEnvOrDefault("DB_NAME", "duplacor");
    private static final String DB_USER = getEnvOrDefault("DB_USER", "duplacor");
    private static final String DB_PASSWORD = getEnvOrDefault("DB_PASSWORD", "duplacor");

    // Construção da URL JDBC
    private static final String URL = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8",
            DB_HOST, DB_PORT, DB_NAME
    );

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC do MySQL não encontrado: " + e.getMessage());
        }
    }

    /**
     * Retorna uma nova conexão com o banco de dados MySQL.
     *
     * @return java.sql.Connection
     * @throws SQLException caso ocorra erro na conexão
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Testa se a conexão com o banco de dados está funcional.
     *
     * @return true se conectou com sucesso, false caso contrário
     */
    public static boolean testarConexao() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Falha ao testar conexão com o banco de dados: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lê variável de ambiente ou retorna valor padrão.
     */
    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    public static String getUrl() {
        return URL;
    }

    public static String getDbUser() {
        return DB_USER;
    }
}
