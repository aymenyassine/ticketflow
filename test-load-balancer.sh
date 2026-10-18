#!/bin/bash

GATEWAY_URL="http://localhost:8080/CUSTOMER-SERVICE/api/customers/instance"

echo "======================================"
echo "   TEST LOAD BALANCER - TICKETFLOW"
echo "======================================"

for i in $(seq 1 10); do
    echo -n "Requete $i -> "

    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" "$GATEWAY_URL")

    status=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS:/d')

    if [ "$status" = "200" ]; then
        port=$(echo "$body" | grep -o '"port":"[^"]*"' | cut -d'"' -f4)
        echo "Customer Service sur le port $port"
    else
        echo "Erreur HTTP $status"
        echo "$body"
    fi

    sleep 0.5
done

echo "======================================"
echo "   TEST TERMINE"
echo "======================================"
