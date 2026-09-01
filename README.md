# Keycloak Kick Identity Provider (SPI)

Extensão de **Social Login** do Keycloak para o **Kick**, permitindo que usuários façam login/cadastro no Keycloak usando sua conta Kick via OAuth 2.1 (Authorization Code Grant + PKCE S256).

Compatível com **Keycloak 26.7.1** (Java 17).

## Endpoints do Kick utilizados

| Endpoint | URL |
|----------|-----|
| Authorization | `https://id.kick.com/oauth/authorize` |
| Token | `https://id.kick.com/oauth/token` |
| User Info | `https://api.kick.com/public/v1/users` |
| Scope padrão | `user:read` |

> **Importante:** A API do Kick exige **PKCE S256** obrigatoriamente. O provider já ativa `pkceEnabled=true` e `pkceMethod=S256` automaticamente; o `code_challenge` / `code_verifier` são gerenciados pelo Keycloak. O host OAuth (`id.kick.com`) é diferente do host da API (`api.kick.com`).

> **Scopes disponíveis (Kick):** `user:read`, `channel:read`, `channel:write`, `chat:write`, `moderation:ban`, `streamkey:read`, etc. Ver https://docs.kick.com/getting-started/generating-tokens-oauth2-flow

## Mapeamento de atributos

O perfil retornado por `GET https://api.kick.com/public/v1/users` (objeto dentro de `data[]`) é mapeado para o `BrokeredIdentityContext`:

| Campo Kick (API) | Destino no Keycloak |
|------------------|---------------------|
| `user_id` | ID federado / `brokerUserId` (convertido para String) |
| `name` | `username` |
| `email` | `email` |
| `user_id` | atributo `kick.user_id` |
| `name` | atributo `kick.name` |
| `email` | atributo `kick.email` |
| `profile_picture` | atributos `kick.profile_picture` e `picture` |

## Build

```bash
mvn clean package
```

O artefato final será gerado em `target/keycloak-kick.jar` (fat jar).

## Deploy

1. Copie `target/keycloak-kick.jar` para o diretório `providers/` do Keycloak:
   ```bash
   cp target/keycloak-kick.jar $KEYCLOAK_HOME/providers/
   ```
2. Reinicie o Keycloak:
   ```bash
   $KEYCLOAK_HOME/bin/kc.sh build
   $KEYCLOAK_HOME/bin/kc.sh start
   # ou para dev: $KEYCLOAK_HOME/bin/kc.sh start-dev
   ```

## Configuração no Keycloak

### 1. Criar a aplicação no Kick

1. Acesse o [Kick Developer Portal](https://kick.com/settings/developer) (ou https://docs.kick.com/getting-started/kick-apps-setup).
2. Crie uma nova App e configure o **Redirect URL** como:
   ```
   https://<SEU_KEYCLOAK>/realms/<SEU_REALM>/broker/kick/endpoint
   ```
3. Anote o **Client ID** e **Client Secret**.

### 2. Configurar o provider no Keycloak

1. No admin console, vá em **Identity Providers → Add provider → Kick**.
2. Preencha:
   - **Client Id**: Client ID da aplicação Kick.
   - **Client Secret**: Client Secret da aplicação Kick.
   - **Default Scopes**: `user:read` (já é o padrão; adicione outros scopes conforme necessário, ex: `user:read channel:read chat:write`).
   - **User Info URL** *(opcional)*: endpoint da API (padrão `https://api.kick.com/public/v1/users`).
   - **Store Tokens**: habilitado por padrão (mantém access/refresh token).
   - **PKCE**: já habilitado como `S256` (não desative, Kick exige).
3. Salve.

### 3. (Opcional) Attribute Mapper

O provider registra o mapper **Kick Attribute Mapper**, que permite mapear qualquer atributo do contexto broker (ex: `kick.name`, `kick.profile_picture`) para um atributo de usuário do Keycloak, configurável via UI sem recompilar.

## Testes

```bash
mvn test
```

Os testes validam a desserialização do payload `{"data": [...]}` da Kick API, cenários com array vazio, ausência de email e campos desconhecidos.

## Licença

MIT