CREATE DATABASE IF NOT EXISTS customer_service;
CREATE DATABASE IF NOT EXISTS booking_service;
CREATE DATABASE IF NOT EXISTS event_service;
GRANT ALL PRIVILEGES ON customer_service.* TO 'ticketflow'@'%';
GRANT ALL PRIVILEGES ON booking_service.* TO 'ticketflow'@'%';
GRANT ALL PRIVILEGES ON event_service.* TO 'ticketflow'@'%';
