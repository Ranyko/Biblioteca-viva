import { Navbar } from '../components/Navbar';
import { LoginForm } from '../components/LoginForm';
import './Login.css';

export function Login() {
  return (
    <div className="app-container">
      <Navbar />
      <main className="main-content">
        <LoginForm />
      </main>
    </div>
  );
}