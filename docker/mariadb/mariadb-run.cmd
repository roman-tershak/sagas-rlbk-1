docker run -d --name mariadb-service ^
    --network vnet-saga-w/rollbacks-1 ^
    -eMYSQL_ROOT_PASSWORD=htgrfedw -eMYSQL_DATABASE=sagarlbk1 -eMYSQL_USER=sagau2 -eMYSQL_PASSWORD=sagagrfedw ^
    mariadb:10.4