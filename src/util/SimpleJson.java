package util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilitário mínimo de JSON (parse e escape) para a API REST nativa,
 * sem dependências externas. Suporta objetos, arrays, strings, números,
 * booleanos e null - suficiente para os corpos de requisição simples da API.
 */
public final class SimpleJson {

    private SimpleJson() {
    }

    // =========================================================================
    // PARSER
    // =========================================================================

    public static Object parse(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        Parser p = new Parser(json);
        p.skipWhitespace();
        Object valor = p.parseValue();
        return valor;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object valor = parse(json);
        if (valor instanceof Map) {
            return (Map<String, Object>) valor;
        }
        return new LinkedHashMap<>();
    }

    private static class Parser {
        private final String s;
        private int i;

        Parser(String s) {
            this.s = s;
            this.i = 0;
        }

        void skipWhitespace() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
                i++;
            }
        }

        Object parseValue() {
            skipWhitespace();
            if (i >= s.length()) return null;
            char c = s.charAt(i);
            if (c == '{') return parseObjectInternal();
            if (c == '[') return parseArrayInternal();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') {
                i += 4; // null
                return null;
            }
            return parseNumber();
        }

        Map<String, Object> parseObjectInternal() {
            Map<String, Object> map = new LinkedHashMap<>();
            i++; // consome '{'
            skipWhitespace();
            if (i < s.length() && s.charAt(i) == '}') {
                i++;
                return map;
            }
            while (i < s.length()) {
                skipWhitespace();
                String chave = parseString();
                skipWhitespace();
                if (i < s.length() && s.charAt(i) == ':') i++;
                Object valor = parseValue();
                map.put(chave, valor);
                skipWhitespace();
                if (i < s.length() && s.charAt(i) == ',') {
                    i++;
                    continue;
                }
                if (i < s.length() && s.charAt(i) == '}') {
                    i++;
                    break;
                }
                break;
            }
            return map;
        }

        List<Object> parseArrayInternal() {
            List<Object> lista = new ArrayList<>();
            i++; // consome '['
            skipWhitespace();
            if (i < s.length() && s.charAt(i) == ']') {
                i++;
                return lista;
            }
            while (i < s.length()) {
                Object valor = parseValue();
                lista.add(valor);
                skipWhitespace();
                if (i < s.length() && s.charAt(i) == ',') {
                    i++;
                    continue;
                }
                if (i < s.length() && s.charAt(i) == ']') {
                    i++;
                    break;
                }
                break;
            }
            return lista;
        }

        String parseString() {
            skipWhitespace();
            if (i >= s.length() || s.charAt(i) != '"') {
                return null;
            }
            i++; // consome '"'
            StringBuilder sb = new StringBuilder();
            while (i < s.length() && s.charAt(i) != '"') {
                char c = s.charAt(i);
                if (c == '\\' && i + 1 < s.length()) {
                    char next = s.charAt(i + 1);
                    switch (next) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'n': sb.append('\n'); break;
                        case 't': sb.append('\t'); break;
                        case 'r': sb.append('\r'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'u':
                            if (i + 5 < s.length()) {
                                String hex = s.substring(i + 2, i + 6);
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            }
                            break;
                        default: sb.append(next);
                    }
                    i += 2;
                } else {
                    sb.append(c);
                    i++;
                }
            }
            i++; // consome '"' final
            return sb.toString();
        }

        Boolean parseBoolean() {
            if (s.startsWith("true", i)) {
                i += 4;
                return Boolean.TRUE;
            }
            if (s.startsWith("false", i)) {
                i += 5;
                return Boolean.FALSE;
            }
            return null;
        }

        Double parseNumber() {
            int inicio = i;
            while (i < s.length() && (Character.isDigit(s.charAt(i)) || s.charAt(i) == '-' || s.charAt(i) == '+' || s.charAt(i) == '.' || s.charAt(i) == 'e' || s.charAt(i) == 'E')) {
                i++;
            }
            String numStr = s.substring(inicio, i);
            if (numStr.isEmpty()) return 0.0;
            try {
                return Double.parseDouble(numStr);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
    }

    // =========================================================================
    // HELPERS DE LEITURA TIPADA (a partir do Map já parseado)
    // =========================================================================

    public static String getString(Map<String, Object> map, String chave) {
        Object v = map.get(chave);
        return v != null ? String.valueOf(v) : null;
    }

    public static Integer getInt(Map<String, Object> map, String chave) {
        Object v = map.get(chave);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        try {
            return Integer.parseInt(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Double getDouble(Map<String, Object> map, String chave) {
        Object v = map.get(chave);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getList(Map<String, Object> map, String chave) {
        Object v = map.get(chave);
        if (v instanceof List) {
            return (List<Object>) v;
        }
        return new ArrayList<>();
    }

    // =========================================================================
    // ESCAPE PARA ESCRITA DE JSON
    // =========================================================================

    public static String escape(String valor) {
        if (valor == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < valor.length(); idx++) {
            char c = valor.charAt(idx);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append(""); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}