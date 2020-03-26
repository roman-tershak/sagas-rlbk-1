docker run --name test-loader ^
    -ti --rm ^
    --network docker_vnet-saga-w/rollbacks-1 ^
    -e CONCURRENCY=10 -e HOLD_FOR=900s -e RAMP_UP=10s ^
    saga-tests-rlbk-1/test-loader:3.0-RLBK-NSTR-SEQEV