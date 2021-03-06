docker run --name test-loader ^
    -ti --rm ^
    --network docker_vnet-saga-w/rollbacks-1 ^
    -e CONCURRENCY=10 -e HOLD_FOR=300s -e RAMP_UP=10s ^
    saga-tests-rlbk-1/test-loader:2.0-RLBK-EV-LOOP