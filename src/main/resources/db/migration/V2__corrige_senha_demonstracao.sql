-- Corrige apenas os hashes de demonstração da V1, sem alterar senhas já trocadas.
UPDATE usuario
SET senha_hash = '$2b$12$Fag2Czl58TQJ.hC9v5qoJuzqNtz9c8x6j8JPIlUbOdXCM4b.UKIyy'
WHERE senha_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';
