#!/usr/bin/env bash

# Create persistent volume for your data
docker volume create prometheus-data

. ../.env
echo "$PROMETHEUS_API_TOKEN" > prometheus_api_token
chmod 600 prometheus_api_token

# Start Prometheus container
docker run \
    -p 9090:9090 \
    -v ./prometheus.yml:/etc/prometheus/prometheus.yml \
    -v ./prometheus_api_token:/etc/prometheus/prometheus_api_token \
    -v prometheus-data:/prometheus \
    prom/prometheus
