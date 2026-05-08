#!/bin/bash
/Users/felixfischer/Developer/livinglink/build/install/livinglink/bin/livinglink \
  | sed -u '/^kotlin-logging: initializing/d'
