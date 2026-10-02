import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export function LoginForm() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  
  // Novas propriedades necessárias para a integração
  const [erro, setErro] = useState('');
  const [carregando, setCarregando] = useState(false);

  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro('');
    setCarregando(true);

    try {
      const response = await fetch('http://localhost:8080/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, senha: password }),
      });

      if (response.ok) {
        const data = await response.json();

        // Salva os dados de autenticação no navegador
        localStorage.setItem('token', data.token);
        localStorage.setItem(
          'usuario',
          JSON.stringify({
            id: data.usuarioId,
            nome: data.nome,
            email: data.email,
            perfil: data.perfil,
          })
        );

        // Redireciona para o Dashboard ou Acervo
        navigate('/dashboard');
      } else {
        setErro('E-mail ou senha incorretos.');
      }
    } catch (err) {
      console.error(err);
      setErro('Erro ao conectar com o servidor.');
    } finally {
      setCarregando(false);
    }
  };

  return (
    <div className="login-card">
      <form onSubmit={handleSubmit}>
        {/* Exibe mensagem de erro caso o login falhe */}
        {erro && <p className="error-message" style={{ color: 'red', marginBottom: '1rem' }}>{erro}</p>}

        <div className="input-group">
          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>

        <div className="input-group">
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>

        <button type="submit" className="btn-primary" disabled={carregando}>
          {carregando ? 'Entrando...' : 'Sign In'}
        </button>
      </form>

      <a href="#forgot" className="forgot-password">
        Forgot password?
      </a>
    </div>
  );
}