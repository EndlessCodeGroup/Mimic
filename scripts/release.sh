#!/usr/bin/env bash

set -e

tag="v${1:?Please specify version}"

git fetch -p
git tag -f "$tag" develop
git push origin "$tag"
