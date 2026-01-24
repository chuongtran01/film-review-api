#!/bin/bash

# Load .env file if it exists and export variables
if [ -f .env ]; then
  export $(cat .env | grep -v '^#' | xargs)
  echo "Loaded environment variables from .env file"
else
  echo "No .env file found. Using system environment variables."
fi

# Run Spring Boot application
./gradlew bootRun
