# .doc - Documentation Keyword Management

Quick reference for Claude Code and AI agents. For detailed documentation, see README.md and doc/ directory.

## Overview

Documentation keyword extraction, validation, and analysis tool. Part of the dev meta-utility family (.adr, .req, .runnote, .doc).

## Core Capabilities

| Command | Purpose | Common Usage |
|---------|---------|--------------|
| `doc-validate` | Validate keywords against taxonomy | Pre-commit hook, CI/CD |
| `doc-search` | Search by keyword/content/cross-refs | Find related docs |
| `doc-index` | Build keyword→file index | IDE integration |
| `doc-graph` | Generate relationship graphs | Visualization |
| `doc-suggest` | Suggest keywords for file | Content analysis |
| `doc-stats` | Usage statistics | Coverage analysis |

## Quick Start

```bash
# Validate current project
doc-validate

# Search for keyword
doc-search keyword :architecture

# Find docs referencing ADR-00042
doc-search adr 00042

# Build index
doc-index > keywords.json

# Generate graph
doc-graph | dot -Tpng > graph.png

# Get suggestions
doc-suggest doc/guide.md

# View stats
doc-stats --top=10
```

## Configuration

**Global defaults:** `~/.doc/config.edn`
**Project overrides:** `.doc.edn` in project root

```edn
{:doc {:path "doc"
       :taxonomy "doc-tools/keyword-taxonomy.md"
       :validation {:strict false}
       :cross-refs {:enable-adr true}}}
```

## Keyword Format

```markdown
# Document Title

Keywords: [:architecture :security :api]

Content with inline keywords [:performance :scalability]
```

## Cross-Reference Integration

Automatically detects:
- ADR references: `[ADR-00042]` or `ADR-00042`
- Requirements: `[REQ-AUTH-001]` or `REQ-AUTH-001`
- RunNotes: `[RunNotes-2025-01-15-Topic]`

Search cross-refs:
```bash
doc-search adr 00042          # Find docs mentioning ADR
doc-search req REQ-AUTH-001   # Find docs mentioning requirement
```

## Taxonomy File

Format in `doc-tools/keyword-taxonomy.md`:

```markdown
# Keyword Taxonomy

## Architecture
- `:architecture` - Architectural decisions
- `:modularity` - Module organization

## Security
- `:security` - Security concerns
- `:authentication` - Auth mechanisms
```

## Multi-Project Analysis

All commands support cross-project analysis:

```bash
doc-validate ~/proj1 ~/proj2 ~/proj3
doc-stats ~/proj1 ~/proj2
doc-index ~/proj1 ~/proj2 > combined-index.json
```

## Common Workflows

### Pre-Commit Validation
```bash
# .git/hooks/pre-commit
doc-validate || exit 1
```

### CI/CD Integration
```bash
# GitHub Actions, etc.
doc-validate
doc-stats --top=20
```

### Find Related Documentation
```bash
# Find docs about architecture
doc-search keyword :architecture

# Find docs referencing specific ADR
doc-search adr 00042

# Find docs mentioning "migration"
doc-search content "database migration"
```

### Keyword Analysis
```bash
# Get suggestions for new doc
doc-suggest --confidence doc/new-guide.md

# View usage patterns
doc-stats

# Build searchable index
doc-index > public/keywords.json
```

### Visualization
```bash
# Generate relationship graph
doc-graph --weights --min-weight=2 | dot -Tpng > keywords.png

# Interactive graph (with D3.js wrapper)
doc-graph > graph.dot
# Process with custom visualization tool
```

## Integration with Other Utilities

### With .adr (Architecture Decision Records)
```bash
# Find docs referencing ADR-00042
doc-search adr 00042

# Validate docs reference valid ADRs
doc-validate  # Checks cross-refs if enabled
```

### With .req (Requirements)
```bash
# Find docs implementing REQ-AUTH-001
doc-search req REQ-AUTH-001
```

### With .runnote (Development Notes)
```bash
# Find docs related to specific RunNotes session
doc-search runnote RunNotes-2025-01-15-Feature
```

## File Organization

```
~/.doc/
├── bin/              # Commands
│   ├── doc-validate
│   ├── doc-search
│   ├── doc-index
│   ├── doc-graph
│   ├── doc-suggest
│   └── doc-stats
├── doc-core.bb       # Core library
├── config.edn        # Global config
└── template/         # Templates

Project root/
├── .doc.edn          # Project config
└── doc/              # Documentation
    └── doc-tools/
        └── keyword-taxonomy.md
```

## Architecture

- **Shared libraries:** Leverages ~/.lib/config-core.bb
- **Three-tier config:** Global < Project < Runtime
- **Cross-utility:** Integrates with .adr, .req, .runnote
- **Decomposed:** Each command is focused and testable

## Exit Codes

- `0` - Success (validation passed, search completed)
- `1` - Failure (validation errors, missing arguments)

## Error Handling

All commands:
- Gracefully handle missing taxonomy (warn, continue)
- Validate file paths before processing
- Provide clear error messages
- Support `--help` flag

## For More Information

- Full documentation: ~/.doc/README.md
- Configuration guide: ~/.doc/doc/README-CONFIG.md (coming soon)
- Workflow examples: ~/.doc/doc/README-WORKFLOWS.md (coming soon)
- File format spec: ~/.doc/doc/README-FORMAT.md (coming soon)

## Version

Version: 1.0.0 (normalized architecture)
Compatible with: .adr v1.0+, .req v1.0+, .runnote v1.0+
