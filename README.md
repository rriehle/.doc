# .doc - Documentation Keyword Management

Documentation keyword extraction, validation, and analysis tool. Part of the dev meta-utility family alongside .adr (Architecture Decision Records), .req (Requirements), and .runnote (Development Notes).

## Features

- **Keyword Extraction**: Extract and index keywords from markdown documentation
- **Validation**: Validate keywords against a project taxonomy
- **Search**: Search by keyword, content, or cross-references (ADR/req/runnote)
- **Visualization**: Generate relationship graphs showing keyword co-occurrence
- **Statistics**: Analyze keyword usage patterns across projects
- **Suggestions**: AI-powered keyword suggestions based on content
- **Multi-Project**: Analyze across multiple projects simultaneously
- **Cross-Utility Integration**: Link to ADRs, requirements, and RunNotes

## Installation

### Prerequisites

- [Babashka](https://babashka.org/) (bb) - Fast Clojure scripting
- [GraphViz](https://graphviz.org/) (dot) - For graph visualization (optional)

### Install

```bash
# Clone or download to ~/.doc
git clone <repo-url> ~/.doc

# Or use install script
curl -sSL <install-url> | bash

# Add to PATH (add to ~/.bashrc or ~/.zshrc)
export PATH="$HOME/.doc/bin:$PATH"

# Verify installation
doc-validate --help
```

## Quick Start

### 1. Add Keywords to Your Docs

```markdown
# API Authentication Guide

Keywords: [:api :authentication :security]

This guide covers authentication mechanisms for our API.

Our approach uses JWT tokens [:jwt :tokens] for stateless authentication.
```

### 2. Create a Taxonomy (Optional)

Create `doc-tools/keyword-taxonomy.md` in your project:

```markdown
# Keyword Taxonomy

## Core Concepts
- `:api` - API design and implementation
- `:authentication` - Authentication mechanisms
- `:security` - Security concerns

## Technical
- `:jwt` - JSON Web Tokens
- `:tokens` - Token-based auth
```

### 3. Validate Your Documentation

```bash
# Validate current project
doc-validate

# Output:
# ✓ All keywords valid (15 files checked)
```

### 4. Search and Analyze

```bash
# Find all docs with :api keyword
doc-search keyword :api

# Find docs referencing ADR-00042
doc-search adr 00042

# Get usage statistics
doc-stats

# Generate visualization
doc-graph | dot -Tpng > keywords.png
```

## Commands

### doc-validate

Validate keywords against taxonomy.

```bash
doc-validate [project ...]

# Examples
doc-validate                    # Current project
doc-validate ~/proj1            # Specific project
doc-validate ~/p1 ~/p2 ~/p3    # Multiple projects
```

**Exit codes:**
- `0` - All keywords valid
- `1` - Invalid keywords found

### doc-search

Search documentation by keyword, content, or cross-references.

```bash
doc-search keyword <keyword> [project ...]      # Search by keyword
doc-search content <text> [project ...]         # Search by content
doc-search adr <id> [project ...]               # Search for ADR refs
doc-search req <id> [project ...]               # Search for req refs
doc-search runnote <id> [project ...]           # Search for RunNotes refs

# Examples
doc-search keyword :architecture
doc-search content "database migration"
doc-search adr 00042
doc-search req REQ-AUTH-001
doc-search runnote RunNotes-2025-01-15-Feature
```

### doc-index

Build keyword index in JSON format.

```bash
doc-index [project ...]

# Examples
doc-index                        # Current project
doc-index > keywords.json        # Save to file
doc-index ~/p1 ~/p2 > index.json # Cross-project index
```

**Output format:**
```json
{
  "architecture": ["guide/arch.md", "adr/ADR-001.md"],
  "security": ["security/overview.md"]
}
```

### doc-graph

Generate keyword relationship graph in GraphViz DOT format.

```bash
doc-graph [options] [project ...]

# Options
--weights              # Show co-occurrence counts
--min-weight=N         # Only edges with weight >= N

# Examples
doc-graph | dot -Tpng > graph.png
doc-graph --weights --min-weight=3
doc-graph ~/p1 ~/p2 > combined.dot
```

### doc-suggest

Suggest keywords for a file based on content analysis.

```bash
doc-suggest [options] <file>

# Options
--confidence           # Show confidence scores
--taxonomy-only        # Only suggest from taxonomy

# Examples
doc-suggest doc/new-guide.md
doc-suggest --confidence doc/api.md
doc-suggest --taxonomy-only doc/security.md
```

### doc-stats

Show keyword usage statistics.

```bash
doc-stats [options] [project ...]

# Options
--top=N                # Show only top N keywords

# Examples
doc-stats
doc-stats --top=10
doc-stats ~/p1 ~/p2 ~/p3
```

## Configuration

### Global Configuration

Edit `~/.doc/config.edn`:

```edn
{:doc {:path "doc"
       :taxonomy "doc-tools/keyword-taxonomy.md"
       :template-dir "~/.doc/template"
       :excluded-patterns #{"README.md" "CHANGELOG.md"}
       :metadata-mode :hybrid
       :validation {:strict false
                    :require-taxonomy false}
       :cross-refs {:enable-adr true
                    :enable-req true
                    :enable-runnote true}}}
```

### Project Configuration

Create `.doc.edn` in your project root to override global settings:

```edn
{:doc {:path "docs"
       :taxonomy "docs/keywords.md"
       :validation {:strict true
                    :require-taxonomy true}}}
```

### Configuration Keys

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `:path` | string | `"doc"` | Documentation directory |
| `:taxonomy` | string | `"doc-tools/keyword-taxonomy.md"` | Taxonomy file path |
| `:excluded-patterns` | set | `#{"README.md" "CHANGELOG.md"}` | Files to exclude |
| `:validation` | map | see below | Validation settings |
| `:cross-refs` | map | see below | Cross-reference integration |

**Validation settings:**
- `:strict` (boolean) - Fail on any validation error
- `:require-taxonomy` (boolean) - Require taxonomy file to exist

**Cross-reference settings:**
- `:enable-adr` (boolean) - Enable ADR cross-references
- `:enable-req` (boolean) - Enable requirement cross-references
- `:enable-runnote` (boolean) - Enable RunNotes cross-references

## Keyword Format

### Inline Keywords

```markdown
Keywords: [:architecture :api :security]

Content with inline [:performance :scalability] keywords.
```

### Syntax

- Bracket notation: `[:keyword1 :keyword2]`
- Colon prefix: `:keyword` or `keyword` (both accepted)
- Kebab-case: `:multi-word-keyword`
- Space-separated within brackets

## Cross-Reference Integration

### Automatic Detection

The tool automatically detects references to:

- **ADRs**: `[ADR-00042]` or `ADR-00042`
- **Requirements**: `[REQ-AUTH-001]` or `REQ-AUTH-001`
- **RunNotes**: `[RunNotes-2025-01-15-Topic]` or `RunNotes-2025-01-15-Topic`

### Search by Reference

```bash
# Find all docs mentioning ADR-00042
doc-search adr 00042

# Find all docs implementing REQ-AUTH-001
doc-search req REQ-AUTH-001

# Find docs related to a RunNotes session
doc-search runnote RunNotes-2025-01-15-Auth
```

## Workflows

### Pre-Commit Validation

Add to `.git/hooks/pre-commit`:

```bash
#!/bin/bash
doc-validate || exit 1
```

### CI/CD Integration

```yaml
# GitHub Actions example
- name: Validate Documentation Keywords
  run: |
    doc-validate
    doc-stats --top=20
```

### Keyword Analysis Workflow

```bash
# 1. Get suggestions for new doc
doc-suggest --confidence docs/new-guide.md

# 2. Add suggested keywords to doc
# (manually edit file)

# 3. Validate
doc-validate

# 4. Check coverage
doc-stats

# 5. Visualize relationships
doc-graph --weights | dot -Tpng > keywords.png
```

### Multi-Project Analysis

```bash
# Validate across all projects
doc-validate ~/proj1 ~/proj2 ~/proj3

# Combined statistics
doc-stats ~/proj1 ~/proj2 ~/proj3

# Cross-project index
doc-index ~/proj1 ~/proj2 > combined-index.json

# Cross-project graph
doc-graph ~/proj1 ~/proj2 --min-weight=5 | dot -Tpng > combined.png
```

## Integration with Other Utilities

### .adr (Architecture Decision Records)

```bash
# Find docs referencing specific ADR
doc-search adr 00042

# Validate ADR references are valid
# (requires ADR integration enabled)
```

### .req (Requirements)

```bash
# Find docs implementing requirement
doc-search req REQ-AUTH-001

# Trace requirement to documentation
```

### .runnote (Development Notes)

```bash
# Find docs mentioned in RunNotes
doc-search runnote RunNotes-2025-01-15-Feature

# Bidirectional linking support
```

## Troubleshooting

### "Taxonomy file not found" Warning

This is normal if you haven't created a taxonomy yet. The tool will skip validation but still extract keywords.

**Solution:** Create `doc-tools/keyword-taxonomy.md` or configure a different path in `.doc.edn`.

### "No files found"

Check your configuration:

```bash
# Verify doc directory exists
ls doc/

# Check configuration
cat .doc.edn
```

### Invalid Keywords Not Detected

Ensure taxonomy file is properly formatted with backtick-colon notation:

```markdown
- `:keyword-name` - Description
```

### Command Not Found

Ensure ~/.doc/bin is in your PATH:

```bash
export PATH="$HOME/.doc/bin:$PATH"
```

## Architecture

### Component Structure

```
~/.doc/
├── bin/              # Executable commands
│   ├── doc-validate
│   ├── doc-search
│   ├── doc-index
│   ├── doc-graph
│   ├── doc-suggest
│   └── doc-stats
├── doc-core.bb       # Core library functions
├── config.edn        # Global defaults
├── template/         # Document templates
├── test/             # Test suite
├── CLAUDE.md         # AI agent quick reference
└── README.md         # This file
```

### Shared Libraries

- `~/.lib/config-core.bb` - Configuration management (shared)
- `~/.lib/metadata-parser.bb` - EDN metadata parsing (shared)

### Design Principles

- **DRY**: No code duplication across commands
- **Composability**: Each command is focused and independent
- **Integration**: Seamless cross-utility references
- **Extensibility**: Easy to add new commands and features

## Contributing

We welcome contributions! Areas for improvement:

- Additional suggestion patterns
- Enhanced visualization options
- IDE integrations
- API documentation
- Additional export formats

## License

MIT License - see LICENSE file

## See Also

- [.adr](https://github.com/YOUR_ORG/adr) - Architecture Decision Records
- [.req](https://github.com/YOUR_ORG/req) - Requirements Management
- [.runnote](https://github.com/YOUR_ORG/runnote) - Development Notes
- [CLAUDE.md](./CLAUDE.md) - AI agent quick reference

## Version

v1.0.0 - Normalized architecture (migrated from doc-keywords)

**Changelog:**
- Migrated from monolithic doc-keywords to decomposed architecture
- Added cross-reference search (ADR/req/runnote)
- Integrated with shared config-core library
- Multi-project support across all commands
- Enhanced visualization with edge weights
