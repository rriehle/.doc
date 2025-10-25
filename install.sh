#!/bin/bash
#
# Documentation Keyword Management (.doc) Tools - Installation Script
# Install .doc tools from GitHub Releases or local source
#
# Usage:
#   curl -sL https://github.com/YOUR_ORG/.doc/releases/latest/download/install.sh | bash
#
#   Or with specific version:
#   curl -sL https://github.com/YOUR_ORG/.doc/releases/download/v1.0.0/install.sh | bash
#
#   Or with custom install directory:
#   curl -sL https://github.com/YOUR_ORG/.doc/releases/latest/download/install.sh | bash -s -- v1.0.0 /custom/path
#
#   Or install from local directory (for development):
#   ./install.sh local
#

set -e

# Configuration
REPO="YOUR_ORG/.doc"
VERSION="${1:-latest}"
INSTALL_DIR="${2:-$HOME/.doc}"
TMP_DIR=$(mktemp -d)

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Cleanup on exit
cleanup() {
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT

# Logging functions
info() {
  echo -e "${GREEN}==>${NC} $1"
}

warn() {
  echo -e "${YELLOW}Warning:${NC} $1"
}

error() {
  echo -e "${RED}Error:${NC} $1" >&2
  exit 1
}

# Check prerequisites
check_prerequisites() {
  if ! command -v bb >/dev/null 2>&1; then
    error "Babashka (bb) is required but not installed.
Install from: https://babashka.org/install"
  fi

  if [ "$VERSION" != "local" ]; then
    if ! command -v curl >/dev/null 2>&1; then
      error "curl is required but not installed"
    fi

    if ! command -v tar >/dev/null 2>&1; then
      error "tar is required but not installed"
    fi
  fi

  # Optional dependencies
  if ! command -v dot >/dev/null 2>&1; then
    warn "GraphViz (dot) not found - graph visualization will not work"
    warn "Install from: https://graphviz.org/download/"
  fi
}

# Check .lib dependency
check_lib_dependency() {
  if [ ! -d "$HOME/.lib" ]; then
    error "Dependency not found: ~/.lib is required

Install it first:
  curl -sL https://github.com/YOUR_ORG/.lib/releases/latest/download/install.sh | bash

Or clone from source:
  git clone https://github.com/YOUR_ORG/.lib ~/.lib"
  fi

  if [ ! -f "$HOME/.lib/config-core.bb" ]; then
    error "~/.lib installation incomplete (missing config-core.bb)"
  fi

  if [ -f "$HOME/.lib/VERSION" ]; then
    LIB_VERSION=$(cat "$HOME/.lib/VERSION")
    info "Found .lib dependency: ${LIB_VERSION}"
  else
    warn ".lib installation missing VERSION file (may be from git clone)"
  fi
}

# Install from local directory (development mode)
install_local() {
  info "Installing from local directory..."

  if [ ! -f "./doc-core.bb" ]; then
    error "Current directory does not appear to be .doc source directory (missing doc-core.bb)"
  fi

  # Remove existing installation
  if [ -d "$INSTALL_DIR" ] && [ "$INSTALL_DIR" != "$(pwd)" ]; then
    warn "Removing existing installation at ${INSTALL_DIR}"
    rm -rf "$INSTALL_DIR"
  fi

  # Copy files to install directory (unless already there)
  if [ "$INSTALL_DIR" != "$(pwd)" ]; then
    info "Copying files to ${INSTALL_DIR}..."
    mkdir -p "$INSTALL_DIR"
    cp -r bin doc-core.bb config.edn template CLAUDE.md README.md LICENSE "$INSTALL_DIR/"

    # Create test directory if it doesn't exist
    mkdir -p "$INSTALL_DIR/test"
    mkdir -p "$INSTALL_DIR/doc"
  fi

  # Make binaries executable
  chmod +x "${INSTALL_DIR}/bin/"*

  info "Local installation complete!"
}

# Determine download URL
get_download_url() {
  if [ "$VERSION" = "latest" ]; then
    # Get latest release version
    info "Fetching latest release version..."
    LATEST_VERSION=$(curl -sL "https://api.github.com/repos/${REPO}/releases/latest" | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/')

    if [ -z "$LATEST_VERSION" ]; then
      error "Could not determine latest version"
    fi

    info "Latest version: ${LATEST_VERSION}"
    echo "https://github.com/${REPO}/releases/download/${LATEST_VERSION}/doc-tools-${LATEST_VERSION}.tar.gz"
  else
    echo "https://github.com/${REPO}/releases/download/${VERSION}/doc-tools-${VERSION}.tar.gz"
  fi
}

# Download and extract
install_from_release() {
  local url=$1

  info "Downloading .doc tools from: ${url}"

  if ! curl -fsSL "$url" -o "${TMP_DIR}/doc-tools.tar.gz"; then
    error "Failed to download .doc tools. Check that the version exists."
  fi

  info "Extracting to ${INSTALL_DIR}..."

  # Create parent directory if it doesn't exist
  mkdir -p "$(dirname "$INSTALL_DIR")"

  # Extract to temporary location
  tar xzf "${TMP_DIR}/doc-tools.tar.gz" -C "$TMP_DIR"

  # Find the extracted directory (should be doc-tools-vX.Y.Z)
  EXTRACTED_DIR=$(find "$TMP_DIR" -maxdepth 1 -type d -name "doc-tools-*" | head -n 1)

  if [ -z "$EXTRACTED_DIR" ]; then
    error "Could not find extracted directory"
  fi

  # Remove existing installation if present
  if [ -d "$INSTALL_DIR" ]; then
    warn "Removing existing installation at ${INSTALL_DIR}"
    rm -rf "$INSTALL_DIR"
  fi

  # Move to final location
  mv "$EXTRACTED_DIR" "$INSTALL_DIR"

  # Make binaries executable
  chmod +x "${INSTALL_DIR}/bin/"*

  info "Installation complete!"
}

# Display post-install instructions
post_install() {
  echo ""
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo ""
  info "Documentation Keyword Management (.doc) Tools installed to: ${INSTALL_DIR}"
  echo ""

  # Check if already in PATH
  if echo "$PATH" | grep -q "${INSTALL_DIR}/bin"; then
    info "✓ Already in PATH"
  else
    echo "Add to your PATH:"
    echo ""

    # Detect shell
    if [ -n "$BASH_VERSION" ]; then
      echo "  echo 'export PATH=\"${INSTALL_DIR}/bin:\$PATH\"' >> ~/.bashrc"
      echo "  source ~/.bashrc"
    elif [ -n "$ZSH_VERSION" ]; then
      echo "  echo 'export PATH=\"${INSTALL_DIR}/bin:\$PATH\"' >> ~/.zshrc"
      echo "  source ~/.zshrc"
    else
      echo "  export PATH=\"${INSTALL_DIR}/bin:\$PATH\""
    fi
    echo ""
  fi

  echo "Verify installation:"
  echo "  doc-validate --help"
  echo "  doc-search --help"
  echo "  doc-stats --help"
  echo ""

  echo "Quick start:"
  echo "  cd your-project/"
  echo "  doc-validate              # Validate keywords"
  echo "  doc-search keyword :api   # Search by keyword"
  echo "  doc-stats                 # View statistics"
  echo ""

  echo "Next steps:"
  echo "  1. Create taxonomy: doc-tools/keyword-taxonomy.md"
  echo "     (See: ${INSTALL_DIR}/template/keyword-taxonomy-example.md)"
  echo "  2. Add keywords to your docs: [:architecture :api :security]"
  echo "  3. Run: doc-validate"
  echo ""
  echo "Documentation:"
  echo "  README: ${INSTALL_DIR}/README.md"
  echo "  AI Quick Reference: ${INSTALL_DIR}/CLAUDE.md"
  echo ""
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo ""
}

# Main
main() {
  info "Installing Documentation Keyword Management (.doc) Tools"
  echo ""

  check_prerequisites
  check_lib_dependency

  if [ "$VERSION" = "local" ]; then
    install_local
  else
    local download_url
    download_url=$(get_download_url)
    install_from_release "$download_url"
  fi

  post_install
}

main "$@"
