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
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "exemplar")
public class Exemplar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @Column(name = "codigo_tombo", nullable = false, unique = true, length = 50)
    private String codigoTombo;

    @Column(name = "estado_conservacao", nullable = false, length = 20)
    private String estadoConservacao;

    @Convert(converter = StatusExemplarConverter.class)
    @Column(nullable = false, length = 20)
    private StatusExemplar status = StatusExemplar.DISPONIVEL;

    @CreationTimestamp
    @Column(name = "cadastrado_em", nullable = false, updatable = false)
    private OffsetDateTime cadastradoEm;

    public Long getId() {
        return id;
    }

    public Livro getLivro() {
        return livro;
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
    }

    public String getCodigoTombo() {
        return codigoTombo;
    }

    public void setCodigoTombo(String codigoTombo) {
        this.codigoTombo = codigoTombo;
    }

    public String getEstadoConservacao() {
        return estadoConservacao;
    }

    public void setEstadoConservacao(String estadoConservacao) {
        this.estadoConservacao = estadoConservacao;
    }

    public StatusExemplar getStatus() {
        return status;
    }

    public void setStatus(StatusExemplar status) {
        this.status = status;
    }

    public OffsetDateTime getCadastradoEm() {
        return cadastradoEm;
    }
}