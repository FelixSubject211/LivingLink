# livinglink

Local Kotlin MCP server for Claude.

## Build

./gradlew installDist

## local.properties

Add your local project path:

projectDir=/absolute/path/to/livinglink

## Claude Desktop config

~/Library/Application Support/Claude/claude_desktop_config.json

{
    "mcpServers": {
        "livinglink": {
            "command": "/absolute/path/to/livinglink/run-claude-mcp.sh"
        }
    }
}

## After changes

./gradlew installDist
pkill Claude