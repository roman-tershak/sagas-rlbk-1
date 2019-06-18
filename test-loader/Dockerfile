FROM blazemeter/taurus
RUN mkdir -p /bzt-configs
COPY src/tests/test-loader.yml /bzt-configs/
WORKDIR /bzt-configs
ENTRYPOINT ["sh", "-c", "bzt -l /tmp/artifacts/bzt.log test-loader.yml \
    -o execution.concurrency=${CONCURRENCY} -o execution.hold-for=${HOLD_FOR} -o execution.ramp-up=${RAMP_UP}"]