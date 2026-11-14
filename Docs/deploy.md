# Deploy da aplicação

O deploy é separado em dois workflows do GitHub Actions:

- [Deploy Backend](../.github/workflows/deploy-backend.yaml)
- [Deploy Frontend](../.github/workflows/deploy-frontend.yaml)

Cada workflow tem um job `build` e um job `deploy`. O build não recebe secrets de SSH; publica um artifact de curta retenção e o job de deploy o baixa e envia à VM. Os workflows podem construir em paralelo. Cada componente tem seu próprio grupo de concorrência para serializar deploys daquele componente sem bloquear o outro.

## Backend

O job de build executa `./mvnw -B -ntp clean package -DskipTests` e publica o JAR como artifact `backend-app`. O job de deploy baixa o artifact e executa [backend.sh](../deploy/backend/backend.sh), que usa as funções SSH compartilhadas de [ssh-common.sh](../deploy/ssh-common.sh). A unit do systemd e configuração Nginx da API ficam em [biblioteca-viva.service](../deploy/backend/biblioteca-viva.service) e [backend-nginx.conf](../deploy/backend/backend-nginx.conf).

Por SSH e SCP, o script transfere o JAR e a unit do systemd. Na VM, o backend é instalado atomicamente em `/opt/biblioteca-viva/app.jar` e executado como `biblioteca-viva.service`. O serviço lê configurações e credenciais de `/etc/biblioteca-viva/backend.env`; somente esse serviço é reiniciado. O script verifica Java 21, a configuração e disponibilidade do PostgreSQL e aguarda a API em `127.0.0.1:8080`.

## Frontend

O job de build executa `npm ci` e `npm run build` no diretório `./frontend`, com `VITE_API_URL` injetada no bundle. O diretório `frontend/dist` é publicado como artifact `frontend-dist`. O job de deploy baixa esse artifact e executa [frontend.sh](../deploy/frontend/frontend.sh), usando a mesma biblioteca SSH do backend. Sua configuração Nginx fica em [frontend-nginx.conf](../deploy/frontend/frontend-nginx.conf).

O script transfere o build e a configuração Nginx exclusiva do site. Na VM, extrai os arquivos em uma área temporária, valida `index.html` e sincroniza o conteúdo para `/var/www/biblioteca-viva` com `rsync --delete`, removendo assets obsoletos. Esse diretório é servido diretamente pelo Nginx; o deploy não usa symlink de versão nem diretórios de releases, e não há rollback automático. O Nginx permanece ativo e recebe reload gracioso.

## Nginx, frontend e API

O Nginx é o proxy reverso público da VM e termina o HTTPS com certificados gerenciados pelo Certbot. Suas configurações são separadas: [frontend-nginx.conf](../deploy/frontend/frontend-nginx.conf) pertence ao deploy do frontend e [backend-nginx.conf](../deploy/backend/backend-nginx.conf) ao deploy do backend. Para `biblioteca-viva.duckdns.org`, serve diretamente os assets estáticos do React em `/var/www/biblioteca-viva`. A rota SPA usa `try_files` com fallback para `index.html`, permitindo rotas do React Router. Os arquivos versionados em `/assets/` recebem cache longo e `immutable`.

Para `api.biblioteca-viva.duckdns.org`, o Nginx encaminha as requisições ao Spring Boot em `127.0.0.1:8080`; a API não fica exposta diretamente na interface pública da VM.

O PostgreSQL roda em um container Docker **na mesma VM** que o backend e o Nginx. O container `biblioteca-postgres` publica a porta somente em `127.0.0.1:5432` e usa o volume persistente `biblioteca-postgres-data`. O deploy do backend verifica o banco, mas nenhum dos workflows o recria ou reinicia.

## Configuração do GitHub Actions

Cadastre estas Actions secrets:

- `DEPLOY_HOST`: endereço DNS ou IP da VM.
- `DEPLOY_USER`: usuário SSH com permissão de `sudo` não interativo para as operações do deploy.
- `DEPLOY_SSH_PRIVATE_KEY`: chave privada usada pelo runner.
- `DEPLOY_SSH_KNOWN_HOSTS`: linha do host e chave pública no formato `known_hosts`, obtida e verificada previamente.

A Actions variable `VITE_API_URL` é opcional; o padrão é `https://api.biblioteca-viva.duckdns.org`.