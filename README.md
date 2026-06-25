# Livinglink

Kotlin Multiplatform app with a Ktor backend (REST + MCP).

This is a personal learning project for me and my family. Users and groups are static configuration (set via env vars and a JSON file) because nobody should be able to register themselves, so static configuration is the simplest thing that works for the current use case.

livinglink is reachable in two ways:

- as an **MCP server** (for Claude, via Cloudflare Tunnel or locally over stdio)
- through a **Kotlin Multiplatform app** (Android, iOS, Web) that talks to the same backend

The web target builds for both JS and Wasm. The Wasm path is experimental.

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

LIVINGLINK_API_KEYS=max:MaxMusterfrau:CHANGE_ME_MAX,anna:AnnaMusterfrau:CHANGE_ME_ANNA

LIVINGLINK_GROUPS_FILE=/absolute/path/to/groups.json

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

LIVINGLINK_GROUPS_FILE=/absolute/path/to/groups.json

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

## Groups

Groups are configured in a JSON file. Set the path via `LIVINGLINK_GROUPS_FILE`.

### `groups.json`

```json
{
  "groups": [
    { "id": "familie", "name": "Familie Musterfrau", "memberUserIds": ["max", "anna"] },
    { "id": "freunde", "name": "Freunde",            "memberUserIds": ["max", "tom"] }
  ]
}
```

Rules:

- Each entry in `memberUserIds` must match a user id. In `http` mode that is the first field of an `LIVINGLINK_API_KEYS` entry; in `stdio` mode it is `LIVINGLINK_STDIO_USER_ID`.
- A user can be in several groups. For MCP, the active group is stored per user in the database and switched via the `set_active_group` tool. `get_session` shows the available groups and which one is active.
- In `stdio` mode the file must contain at least one group whose `memberUserIds` includes your `LIVINGLINK_STDIO_USER_ID`.

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