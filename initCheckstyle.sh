#!/bin/bash

CONFIG_DIR="./config/checkstyle"

CHECKSTYLE_VERSION="10.18.1"
CHECKSTYLE_CONFIG_URL="https://raw.githubusercontent.com/checkstyle/checkstyle/checkstyle-${CHECKSTYLE_VERSION}/src/main/resources/google_checks.xml"

mkdir -p $CONFIG_DIR

echo "Install checkstyle.xml from $CHECKSTYLE_CONFIG_URL..."
curl -L -o "$CONFIG_DIR/checkstyle.xml" $CHECKSTYLE_CONFIG_URL

# shellcheck disable=SC2181
if [[ $? -eq 0 ]]; then
    echo "checkstyle.xml installed in $CONFIG_DIR."
else
    echo "error"
fi
