docker run -d --name mariadb-service ^
    --network vnet-saga-w/rollbacks-1 ^
    -p 3306:3306 ^
    -eMYSQL_ROOT_PASSWORD=htgrfedw -eMYSQL_DATABASE=sagarlbk1 -eMYSQL_USER=sagau2 -eMYSQL_PASSWORD=sagagrfedw ^
    mariadb:10.4