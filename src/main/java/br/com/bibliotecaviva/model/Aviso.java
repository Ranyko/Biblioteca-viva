package br.com.bibliotecaviva.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "aviso")
public class Aviso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 30, nullable = false)
    private TipoAviso tipo;

    @Column(name = "titulo", length = 120, nullable = false)
    private String titulo;

    @Column(name = "mensagem", length = 500, nullable = false)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(name = "referencia_tipo", length = 30)
    private ReferenciaTipo referenciaTipo;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(name = "dia_referencia")
    private LocalDate diaReferencia;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "lido_em")
    private OffsetDateTime lidoEm;

    public enum TipoAviso {
        DEVIDA_AMANHA, VENCIDO, RESERVA_DISPONIVEL, RESERVA_EXPIRANDO,
        RESERVA_EXPIRADA, MULTA_GERADA, FILA_POSICAO
    }

    public enum ReferenciaTipo {
        emprestimo, reserva, multa
    }

    @PrePersist
    protected void onCreate() {
        if (this.criadoEm == null) {
            this.criadoEm = OffsetDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (lidoEm != null && criadoEm != null && lidoEm.isBefore(criadoEm)) {
            throw new IllegalStateException("A data de leitura não pode ser anterior à data de criação.");
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public TipoAviso getTipo() { return tipo; }
    public void setTipo(TipoAviso tipo) { this.tipo = tipo; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public ReferenciaTipo getReferenciaTipo() { return referenciaTipo; }
    public void setReferenciaTipo(ReferenciaTipo referenciaTipo) { this.referenciaTipo = referenciaTipo; }
    public Long getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Long referenciaId) { this.referenciaId = referenciaId; }
    public LocalDate getDiaReferencia() { return diaReferencia; }
    public void setDiaReferencia(LocalDate diaReferencia) { this.diaReferencia = diaReferencia; }
    public OffsetDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(OffsetDateTime criadoEm) { this.criadoEm = criadoEm; }
    public OffsetDateTime getLidoEm() { return lidoEm; }
    public void setLidoEm(OffsetDateTime lidoEm) { this.lidoEm = lidoEm; }
}

