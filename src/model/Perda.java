package model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa a entidade Perda no sistema Dupla Cor (Auditoria de perdas por validade/avarias).
 * Mapeamento da tabela: Perda
 */
public class Perda {

    private int idPerda;
    private Integer quantidade;
    private LocalDateTime dataRegistro;
    private String motivo;
    private int loteId;
    private Lote lote;

    // Construtor vazio
    public Perda() {
    }

    // Construtor completo com ID (leitura do banco)
    public Perda(int idPerda, Integer quantidade, LocalDateTime dataRegistro, String motivo, int loteId) {
        this.idPerda = idPerda;
        this.quantidade = quantidade;
        this.dataRegistro = dataRegistro;
        this.motivo = motivo;
        this.loteId = loteId;
    }

    // Construtor completo com Lote objeto
    public Perda(int idPerda, Integer quantidade, LocalDateTime dataRegistro, String motivo, Lote lote) {
        this.idPerda = idPerda;
        this.quantidade = quantidade;
        this.dataRegistro = dataRegistro;
        this.motivo = motivo;
        this.lote = lote;
        if (lote != null) {
            this.loteId = lote.getIdLote();
        }
    }

    // Construtor para inserção (sem ID)
    public Perda(Integer quantidade, String motivo, Lote lote) {
        this.quantidade = quantidade;
        this.dataRegistro = LocalDateTime.now();
        this.motivo = motivo;
        this.lote = lote;
        if (lote != null) {
            this.loteId = lote.getIdLote();
        }
    }

    // Construtor para inserção com loteId
    public Perda(Integer quantidade, String motivo, int loteId) {
        this.quantidade = quantidade;
        this.dataRegistro = LocalDateTime.now();
        this.motivo = motivo;
        this.loteId = loteId;
    }

    // Getters e Setters

    public int getIdPerda() {
        return idPerda;
    }

    public void setIdPerda(int idPerda) {
        this.idPerda = idPerda;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public int getLoteId() {
        if (lote != null) {
            return lote.getIdLote();
        }
        return loteId;
    }

    public void setLoteId(int loteId) {
        this.loteId = loteId;
    }

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
        if (lote != null) {
            this.loteId = lote.getIdLote();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Perda perda = (Perda) o;
        return idPerda == perda.idPerda;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPerda);
    }

    @Override
    public String toString() {
        return "Perda{" +
                "idPerda=" + idPerda +
                ", quantidade=" + quantidade +
                ", dataRegistro=" + dataRegistro +
                ", motivo='" + motivo + '\'' +
                ", loteId=" + getLoteId() +
                (lote != null ? ", lote=" + lote.getIdLote() : "") +
                '}';
    }
}