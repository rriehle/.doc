# Migration Guide: doc-keywords → .doc

This guide helps you migrate from the monolithic `doc-keywords` utility to the normalized `.doc` architecture.

## Overview

The `.doc` utility is a complete architectural normalization of `doc-keywords`, providing:
- ✅ Decomposed commands (6 focused commands vs 1 monolith)
- ✅ Shared library integration (config-core.bb)
- ✅ Three-tier configuration (global < project < runtime)
- ✅ Cross-utility integration (ADR, requirements, RunNotes)
- ✅ Enhanced search capabilities
- ✅ Consistent patterns with .adr, .req, .runnote

## Migration Strategy

**Approach: Clean cutover** - The new utility lives at `~/.doc` and the old one remains at `~/src/doc-keywords` during transition.

## Pre-Migration Checklist

- [ ] Backup current `~/src/doc-keywords` setup
- [ ] Verify `.lib` dependency is installed (`~/.lib/config-core.bb` exists)
- [ ] Note any custom configurations in `.doc-keywords.edn` files
- [ ] Document current workflows using doc-keywords

## Step-by-Step Migration

### 1. Install .doc

```bash
# From the normalized .doc directory
cd ~/.doc
./install.sh local

# Or from releases (when available)
curl -sL <install-url> | bash
```

### 2. Verify Installation

```bash
# Check commands are available
doc-validate --help
doc-search --help
doc-index --help
doc-graph --help
doc-suggest --help
doc-stats --help
```

### 3. Migrate Configuration

#### Old Format (`.doc-keywords.edn`)
```edn
{:doc-dir "doc"
 :taxonomy "doc-tools/keyword-taxonomy.md"}
```

#### New Format (`.doc.edn`)
```edn
{:doc {:path "doc"
       :taxonomy "doc-tools/keyword-taxonomy.md"}}
```

**Migration script:**
```bash
# For each project with .doc-keywords.edn
cd your-project/
if [ -f .doc-keywords.edn ]; then
  # Read old config and create new one
  echo '{:doc' > .doc.edn
  tail -n +1 .doc-keywords.edn >> .doc.edn
  echo '}' >> .doc.edn

  # Or manually edit to nest under :doc key
fi
```

### 4. Update Commands

| Old Command | New Command | Notes |
|-------------|-------------|-------|
| `doc-keywords extract-keywords <file>` | `doc-search keyword :term <file>` | Reimplemented as search |
| `doc-keywords keyword-index [projects]` | `doc-index [projects]` | ✅ Compatible |
| `doc-keywords keyword-graph [projects]` | `doc-graph [projects]` | ✅ Compatible, new options |
| `doc-keywords validate-keywords [projects]` | `doc-validate [projects]` | ✅ Compatible |
| `doc-keywords suggest-keywords <file>` | `doc-suggest <file>` | ✅ Compatible, new options |
| `doc-keywords keyword-stats [projects]` | `doc-stats [projects]` | ✅ Compatible, new options |

### 5. Update Scripts and Workflows

#### Git Hooks

**Old (.git/hooks/pre-commit):**
```bash
#!/bin/bash
doc-keywords validate-keywords || exit 1
```

**New:**
```bash
#!/bin/bash
doc-validate || exit 1
```

#### CI/CD

**Old (GitHub Actions):**
```yaml
- name: Validate Keywords
  run: doc-keywords validate-keywords
```

**New:**
```yaml
- name: Validate Keywords
  run: doc-validate
```

#### Shell Scripts

**Old:**
```bash
doc-keywords keyword-stats ~/proj1 ~/proj2
doc-keywords keyword-graph | dot -Tpng > graph.png
```

**New:**
```bash
doc-stats ~/proj1 ~/proj2
doc-graph | dot -Tpng > graph.png
```

### 6. Test Migration

Run these verification tests:

```bash
# 1. Validate works
cd your-project/
doc-validate
# Should output: ✓ All keywords valid (N files checked)

# 2. Search works
doc-search keyword :architecture
# Should list files with :architecture

# 3. Stats work
doc-stats
# Should show keyword frequency table

# 4. Index works
doc-index > /tmp/test-index.json
cat /tmp/test-index.json
# Should show JSON index

# 5. Graph works
doc-graph > /tmp/test-graph.dot
dot -Tpng /tmp/test-graph.dot > /tmp/test-graph.png
# Should generate graph

# 6. Suggest works
doc-suggest doc/some-file.md
# Should show suggestions
```

### 7. Update PATH

```bash
# Remove old path (if added)
# Edit ~/.bashrc or ~/.zshrc and remove:
# export PATH="$HOME/src/doc-keywords:$PATH"

# Add new path
echo 'export PATH="$HOME/.doc/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc

# Verify
which doc-validate
# Should output: /Users/yourname/.doc/bin/doc-validate
```

## New Features

### 1. Enhanced Search

**Cross-reference search (NEW):**
```bash
# Find docs referencing ADR-00042
doc-search adr 00042

# Find docs referencing REQ-AUTH-001
doc-search req REQ-AUTH-001

# Find docs mentioning RunNotes session
doc-search runnote RunNotes-2025-01-15-Feature
```

**Content search (NEW):**
```bash
# Find docs containing text
doc-search content "database migration"
```

### 2. Graph Options

**Edge weights (NEW):**
```bash
# Show co-occurrence counts
doc-graph --weights

# Filter weak relationships
doc-graph --min-weight=3
```

### 3. Suggestion Options

**Confidence scores (NEW):**
```bash
# Show confidence percentages
doc-suggest --confidence doc/guide.md
```

**Taxonomy filtering (NEW):**
```bash
# Only suggest keywords from taxonomy
doc-suggest --taxonomy-only doc/guide.md
```

### 4. Stats Options

**Top-N filtering (NEW):**
```bash
# Show only top 10 keywords
doc-stats --top=10
```

## Breaking Changes

### Configuration Format

**Change:** Configuration must be nested under `:doc` key

**Impact:** Projects with `.doc-keywords.edn` must update to `.doc.edn`

**Migration:** See Step 3 above

### Extract Keywords Command

**Change:** Removed as standalone command

**Alternative:** Use `doc-search keyword :term` or process with doc-index

**Rationale:** Extracting keywords is now part of search and indexing workflows

## Compatibility Matrix

| Feature | doc-keywords | .doc | Notes |
|---------|--------------|------|-------|
| Keyword validation | ✅ | ✅ | Compatible |
| Keyword index | ✅ | ✅ | Compatible |
| Keyword graph | ✅ | ✅ | Enhanced with weights |
| Keyword stats | ✅ | ✅ | Enhanced with top-N |
| Keyword suggestions | ✅ | ✅ | Enhanced with confidence |
| Cross-ref search | ❌ | ✅ | **NEW** |
| Content search | ❌ | ✅ | **NEW** |
| Multi-project | ✅ | ✅ | Compatible |
| Taxonomy validation | ✅ | ✅ | Compatible |

## Rollback Plan

If you need to rollback to doc-keywords:

```bash
# 1. Remove ~/.doc from PATH
# Edit ~/.bashrc or ~/.zshrc

# 2. Re-add ~/src/doc-keywords to PATH
export PATH="$HOME/src/doc-keywords:$PATH"

# 3. Revert configuration files
cd your-project/
mv .doc.edn .doc-keywords.edn
# Manually edit to remove :doc nesting

# 4. Update scripts to use old commands
# Replace doc-validate with doc-keywords validate-keywords
# etc.
```

## Troubleshooting

### "Command not found: doc-validate"

**Issue:** PATH not updated or installation incomplete

**Solution:**
```bash
export PATH="$HOME/.doc/bin:$PATH"
which doc-validate
```

### "Dependency not found: ~/.lib is required"

**Issue:** Shared library not installed

**Solution:**
```bash
# Install .lib dependency
curl -sL <lib-install-url> | bash

# Or clone from source
git clone <lib-repo-url> ~/.lib
```

### Configuration not recognized

**Issue:** Configuration not nested under `:doc` key

**Solution:** Ensure `.doc.edn` has structure:
```edn
{:doc {:path "doc" ...}}
```

### Commands behave differently

**Issue:** Different argument order or options

**Solution:** Check command help: `doc-<command> --help`

## Support

- **Documentation:** ~/.doc/README.md
- **Quick reference:** ~/.doc/CLAUDE.md
- **Example taxonomy:** ~/.doc/template/keyword-taxonomy-example.md
- **GitHub issues:** <repo-url>/issues

## Post-Migration

### Optional Cleanup

After verifying migration success (recommend waiting 1-2 weeks):

```bash
# Archive old installation
mv ~/src/doc-keywords ~/src/doc-keywords.backup

# Or remove entirely
rm -rf ~/src/doc-keywords
```

### Update Documentation

Update any project-specific documentation mentioning doc-keywords:

- README files
- Contribution guides
- Development workflows
- CI/CD documentation

## Version Information

- **doc-keywords:** v0.9.x (monolithic)
- **.doc:** v1.0.0 (normalized architecture)
- **Migration completed:** [Date]

## Feedback

If you encounter issues during migration:

1. Check this guide's troubleshooting section
2. Review ~/.doc/README.md
3. File an issue at <repo-url>/issues
4. Contact: [maintainer]

---

**Migration Status Checklist:**

- [ ] .doc installed and verified
- [ ] Configuration migrated (`.doc.edn`)
- [ ] Scripts updated (git hooks, CI/CD)
- [ ] PATH updated
- [ ] All commands tested
- [ ] Team notified
- [ ] Documentation updated
- [ ] Old installation archived (after verification period)

**Estimated migration time:** 15-30 minutes per project
