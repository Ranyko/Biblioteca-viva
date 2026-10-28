package br.com.bibliotecaviva.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leitor_id", nullable = false)
    private Leitor leitor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exemplar_id")
    private Exemplar exemplar;

    @Column(name = "data_solicitacao", nullable = false)
    private OffsetDateTime dataSolicitacao;

    @Convert(converter = StatusReservaConverter.class)
    @Column(nullable = false, length = 20)
    private StatusReserva status = StatusReserva.ATIVA;

    @Column(name = "data_disponibilizacao")
    private OffsetDateTime dataDisponibilizacao;

    @Column(name = "data_limite_retirada")
    private OffsetDateTime dataLimiteRetirada;

    public Long getId() {
        return id;
    }

    public Leitor getLeitor() {
        return leitor;
    }

    public void setLeitor(Leitor leitor) {
        this.leitor = leitor;
    }

    public Livro getLivro() {
        return livro;
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
    }

    public Exemplar getExemplar() {
        return exemplar;
    }

    public void setExemplar(Exemplar exemplar) {
        this.exemplar = exemplar;
    }

    public OffsetDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(OffsetDateTime dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public StatusReserva getStatus() {
        return status;
    }

    public void setStatus(StatusReserva status) {
        this.status = status;
    }

    public OffsetDateTime getDataDisponibilizacao() {
        return dataDisponibilizacao;
    }

    public void setDataDisponibilizacao(OffsetDateTime dataDisponibilizacao) {
        this.dataDisponibilizacao = dataDisponibilizacao;
    }

    public OffsetDateTime getDataLimiteRetirada() {
        return dataLimiteRetirada;
    }

    public void setDataLimiteRetirada(OffsetDateTime dataLimiteRetirada) {
        this.dataLimiteRetirada = dataLimiteRetirada;
    }
}