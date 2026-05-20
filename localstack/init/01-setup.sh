#!/bin/bash
awslocal sqs create-queue --queue-name access-count-queue
awslocal secretsmanager create-secret \
    --name /url-shortener/feistel-salt \
    --secret-string "local-dev-salt-change-in-prod"
awslocal secretsmanager create-secret \
    --name /url-shortener/db-password \
    --secret-string "postgres"
