# livinglink

Kotlin MCP server.

This is a personal learning project for me and my family. Everyone is effectively in one shared group. Authentication uses static API keys per user (configured via env vars) because nobody should be able to register themselves, and proper auth flows with Claude are still a bit awkward today, so static keys are the simplest thing that works for the current use case.

livinglink is reachable in two ways:

- as an **MCP server** (for Claude, via Cloudflare Tunnel or locally over stdio)
- through a **Kotlin Multiplatform app** (Android, iOS, Web) that talks to the same backend

## Modes

livinglink can run in two modes:

- `http`: Docker + Cloudflare Tunnel
- `stdio`: local Claude Desktop

Create a `.env` file in the project root for the mode you want to use.

## Mode 1: HTTP with Cloudflare Tunnel

Use this mode for the public Cloudflare URL.

### `.env`

```env
LIVINGLINK_MCP_TRANSPORT=http

LIVINGLINK_HTTP_HOST=0.0.0.0
LIVINGLINK_HTTP_PORT=3001

LIVINGLINK_MCP_HTTP_HOST=0.0.0.0
LIVINGLINK_MCP_HTTP_PORT=3000
LIVINGLINK_MCP_HTTP_PATH=/mcp
LIVINGLINK_MCP_API_KEYS=max:MaxMusterfrau:CHANGE_ME_MAX,anna:AnnaMusterfrau:CHANGE_ME_ANNA

LIVINGLINK_MONGO_CONNECTION_STRING=mongodb://mongo:27017
LIVINGLINK_MONGO_DATABASE=livinglink

LIVINGLINK_TIMEZONE=Europe/Berlin

CLOUDFLARE_TUNNEL_TOKEN=CHANGE_ME_CLOUDFLARE_TUNNEL_TOKEN
```

### Start

```bash
docker compose \
  --env-file .env \
  -f docker-compose.yml \
  -f docker-compose.app.yml \
  -f docker-compose.web.yml \
  -f docker-compose.cloudflare.yml \
  up --build
```

### MCP URL

Use your Cloudflare domain, `/mcp`, and the API key as `key`.

```text
https://your-cloudflare-domain.example/mcp?key=CHANGE_ME_MAX
```

## Mode 2: Stdio for Claude Desktop

Use this mode when Claude Desktop starts livinglink locally through `run-claude-mcp.sh`.

### `.env`

```env
LIVINGLINK_MCP_TRANSPORT=stdio

LIVINGLINK_STDIO_USER_ID=max
LIVINGLINK_STDIO_USERNAME=MaxMusterfrau

LIVINGLINK_MONGO_CONNECTION_STRING=mongodb://localhost:27017
LIVINGLINK_MONGO_DATABASE=livinglink

LIVINGLINK_TIMEZONE=Europe/Berlin
```

### Start MongoDB

```bash
docker compose up -d
```

### Build

```bash
./gradlew installDist
```

## local.properties

Create `local.properties` in the project root:

```properties
# Server: absolute path to the project
projectDir=/absolute/path/to/livinglink

# App: base URL the client app uses to reach the backend
livinglink.baseUrl=https://your-cloudflare-domain.example
```

## Claude Desktop config

### macOS

Edit this file:

```text
~/Library/Application Support/Claude/claude_desktop_config.json
```

Example:

```json
{
    "mcpServers": {
        "livinglink": {
            "command": "/absolute/path/to/livinglink/run-claude-mcp.sh"
        }
    }
}
```

After changing Kotlin code, rebuild:

```bash
./gradlew installDist
```

## Lint

Check formatting:

```bash
./gradlew ktlintCheck
```

Auto-format:

```bash
./gradlew ktlintFormat
```