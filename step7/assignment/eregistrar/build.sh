#!/bin/bash
docker build -t eregistrar .
docker run -p 8081:8081 eregistrar
