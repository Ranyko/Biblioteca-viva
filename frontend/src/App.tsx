import type { ReactNode } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { RequireRole } from './components/RequireRole';
import type { Perfil } from './services/usuarios';
import { Login } from './pages/Login';
import { CadastroUsuario } from './pages/CadastroUsuario';
import { GerenciarUsuarios } from './pages/GerenciarUsuarios';
import { Dashboard } from './pages/Dashboard';
import { Emprestimo } from "./pages/Emprestimo";
import { Devolucao } from "./pages/Devolucao";
import { Reserva } from './pages/Reserva';
import { Acervo } from './pages/Acervo';
import { FilaReserva } from './pages/FilaReserva';
import { CadastrarLivro } from './pages/CadastrarLivro';
import { GerenciarLivros } from './pages/GerenciarLivros';
import { GerenciarExemplares } from './pages/GerenciarExemplares';

const ADMIN: Perfil[] = ['administrador'];
const ATENDENTE: Perfil[] = ['atendente'];
const LEITOR: Perfil[] = ['leitor'];

// Permissões por tela, conforme o Roteiro do Protótipo Navegável.
function protegido(perfis: Perfil[], tela: ReactNode) {
  return <RequireRole perfis={perfis}>{tela}</RequireRole>;
}

export function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Login />} />

        {/* Administrador */}
        <Route path="/dashboard" element={protegido(ADMIN, <Dashboard />)} />
        <Route path="/usuarios" element={protegido(ADMIN, <GerenciarUsuarios />)} />
        <Route path="/cadastrar-usuario" element={protegido(ADMIN, <CadastroUsuario />)} />
        <Route path="/livros" element={protegido(ADMIN, <GerenciarLivros />)} />
        <Route path="/livros/cadastrar" element={protegido(ADMIN, <CadastrarLivro />)} />
        <Route path="/livros/:id/editar" element={protegido(ADMIN, <CadastrarLivro />)} />
        <Route path="/exemplares" element={protegido(ADMIN, <GerenciarExemplares />)} />

        {/* Atendente */}
        <Route path="/emprestimo" element={protegido(ATENDENTE, <Emprestimo />)} />
        <Route path="/devolucao" element={protegido(ATENDENTE, <Devolucao />)} />
        <Route path="/filareserva" element={protegido(ATENDENTE, <FilaReserva />)} />

        {/* Leitor */}
        <Route path="/acervo" element={protegido(LEITOR, <Acervo />)} />
        <Route path="/reserva" element={protegido(LEITOR, <Reserva />)} />

        <Route path="*" element={<h2 style={{ padding: '2rem' }}>Página não encontrada</h2>} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;