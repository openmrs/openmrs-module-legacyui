# OpenMRS Legacy UI Module - Docker Setup

## Prerequisites
- Docker
- Docker Compose
- Maven (for building the module)

## Quick Start

### 1. Clone and Build
```bash
git clone <repository-url>
cd openmrs-module-legacyui
mvn clean install -DskipTests
```

### 2. Start Services
```bash
docker-compose -f docker-compose-openmrs.yml up -d
```

### 3. Access OpenMRS
- URL: http://localhost:8080/openmrs
- Username: `admin`
- Password: `Admin123`

## Services

| Service | Port | Description |
|---------|------|-------------|
| OpenMRS | 8080 | Main application |
| MySQL | 3306 | Database |

## Useful Commands

### View Logs
```bash
# OpenMRS logs
docker logs openmrs-module-legacyui_openmrs_1 -f

# MySQL logs
docker logs openmrs-module-legacyui_mysql_1 -f
```

### Stop Services
```bash
docker-compose -f docker-compose-openmrs.yml down
```

### Rebuild Module
```bash
mvn clean install -DskipTests
docker-compose -f docker-compose-openmrs.yml restart openmrs
```

### MySQL Access
```bash
docker exec -it openmrs-module-legacyui_mysql_1 mysql -u openmrs -pAdmin123 openmrs
```

## Database Configuration
- Database: `openmrs`
- Username: `openmrs` 
- Password: `Admin123`
- MySQL Profiling: Enabled

## Troubleshooting

### OpenMRS not starting
1. Check logs: `docker logs openmrs-module-legacyui_openmrs_1`
2. Ensure MySQL is ready: `docker logs openmrs-module-legacyui_mysql_1`
3. Restart services: `docker-compose -f docker-compose-openmrs.yml restart`

### Module not loading
1. Rebuild: `mvn clean install -DskipTests`
2. Restart OpenMRS: `docker-compose -f docker-compose-openmrs.yml restart openmrs`

### Port conflicts
Change ports in `docker-compose-openmrs.yml`:
```yaml
ports:
  - "8081:8080"  # OpenMRS
  - "3307:3306"  # MySQL
```