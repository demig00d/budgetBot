#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username budgetbot --dbname budgetbot_data <<-EOSQL
  CREATE TABLE IF NOT EXISTS budget_state (
    chat_id BIGINT  NOT NULL PRIMARY KEY,
    balance INTEGER NOT NULL DEFAULT 0,
    history TEXT
  );
EOSQL

