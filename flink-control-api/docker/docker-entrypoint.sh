#!/usr/bin/env bash

set -eux

CONF_FILE="${FLINK_HOME}/conf/flink-conf.yaml"

set_config_option() {
  local option=$1
  local value=$2

  # escape periods for usage in regular expressions
  local escaped_option=$(echo ${option} | sed -e "s/\./\\\./g")

  # either override an existing entry, or append a new one
  if grep -E "^${escaped_option}:.*" "${CONF_FILE}" > /dev/null; then
        sed -i -e "s/${escaped_option}:.*/$option: $value/g" "${CONF_FILE}"
  else
        echo "${option}: ${value}" >> "${CONF_FILE}"
  fi
}

echo "JOB MANAGER: $JOB_MANAGER_RPC_ADDRESS"
#set_config_option jobmanager.rpc.address ${JOB_MANAGER_RPC_ADDRESS}
cat $CONF_FILE

exec "/docker-entrypoint.sh" "$@"
