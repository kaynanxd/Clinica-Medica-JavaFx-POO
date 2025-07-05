public abstract class Usuario {
    protected int id;
    protected String senha;
    protected String nome;
    protected String tipoUsuario;

    protected Usuario( String senha, String nome, String tipoUsuario) {
        this.senha = senha;
        this.nome = nome;
        this.tipoUsuario = tipoUsuario;
    }
    // --- Getters ---
    public int getId() {return id;}
    public String getSenha() {return senha;}
    public String getNome() {return nome;}
    public String getTipoUsuario() {return tipoUsuario;}

    // --- Setters ---
    public void setId(int id) {this.id = id;}
    public void setNome(String nome) {this.nome = nome;}
    public void setSenha(String senha) {this.senha = senha;}
}