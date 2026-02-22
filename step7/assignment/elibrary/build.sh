#!/bin/bash
docker build -t elibrary .
docker run -p 8080:8080 elibrary
