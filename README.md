# Cachly Brain — IntelliJ Plugin

AI Brain health monitor for [Cachly](https://cachly.dev). Shows lesson count, recall stats, and estimated token savings in the IntelliJ status bar.

## Features

- 🧠 **Status bar widget** — live lesson count and brain health
- 📖 **Lesson viewer** — see all learned lessons with recall counts, severity, and what worked
- 💰 **Token savings** — estimates how many tokens (and dollars) the brain saves by reusing solutions
- ⚙️ **Settings panel** — configure API key, instance ID, and refresh interval under Tools → Cachly Brain

## Installation

### From JetBrains Marketplace (coming soon)

Search for "Cachly Brain" in **Settings → Plugins → Marketplace**.

### Manual Install

1. Download the `.zip` from [GitHub Releases](https://github.com/HeinrichNebula/cachly/releases)
2. **Settings → Plugins → ⚙️ → Install Plugin from Disk…**
3. Select the `.zip` file

## Configuration

Go to **Settings → Tools → Cachly Brain** and enter:

| Setting | Description |
|---------|-------------|
| API Key | Your Cachly API key (`cky_live_...`) |
| Instance ID | Your Brain instance UUID |
| API URL | `https://api.cachly.dev` (default) |
| Refresh Interval | Status bar refresh in seconds (default: 300) |

## How Lessons Work

Lessons are created when an AI assistant (GitHub Copilot, Claude, Cursor) calls `learn_from_attempts()` via the [Cachly MCP server](https://github.com/HeinrichNebula/cachly/tree/main/sdk/mcp). Each recall via `recall_best_solution()` or `session_start()` reuses a known solution instead of re-researching, saving ~1,200 tokens per recall.

## Build

```bash
cd sdk/intellij
./gradlew buildPlugin
# Output: build/distributions/cachly-brain-intellij-0.1.0.zip
```

## License

MIT — see [LICENSE](../../LICENSE)
