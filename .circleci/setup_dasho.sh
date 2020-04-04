#!/usr/bin/env bash

# Make sure we are runing in CI
if [[ -z "$CI" ]]; then echo "Must be run from the CI environment" 1>&2; exit 1; fi
if [[ -z "$DASHO_S3_URI" ]]; then echo "DASHO_S3_URI must be set" 1>&2; exit 1; fi
if [[ -z "$DASHO_CONFIG_S3_URI" ]]; then echo "DASHO_CONFIG_S3_URI must be set" 1>&2; exit 1; fi

set -xeuo pipefail

# Go to the top-level directory.
REPO_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )"/../.. && pwd )"
cd "${REPO_PATH}" || exit

DASHO_HOME="${HOME}"/dasho

install_dasho() {
    local NAME
    NAME=$(basename "${DASHO_S3_URI}")
    aws s3 cp "${DASHO_S3_URI}" .
    mkdir -p "${DASHO_HOME}"
    tar xzf "$NAME" -C "${DASHO_HOME}" --strip-components=1
    rm "$NAME"
}

# DashO needs to be configured via the GUI before using the CLI. dasho_data.tar.gz
# contains the data written after configuring via the GUI.
configure_dasho() {
    local NAME
    NAME=$(basename "${DASHO_CONFIG_S3_URI}")
    aws s3 cp "${DASHO_CONFIG_S3_URI}" .
    local DEST="${HOME}/.preemptivesolutions/dasho/"
    mkdir -p "$DEST"
    tar xzf "$NAME" -C "$DEST"
    rm "$NAME"
}

install_dasho
configure_dasho

echo "export DASHO_HOME=\"${DASHO_HOME}\"" >> "${BASH_ENV}"
