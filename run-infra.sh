#!/bin/bash

# Script para manejar la infraestructura con Locust

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Función para mostrar ayuda
show_help() {
    echo "Uso: $0 [OPCIÓN]"
    echo ""
    echo "Opciones:"
    echo "  up              Levanta toda la infraestructura (DB + Backend + Locust)"
    echo "  down            Baja toda la infraestructura"
    echo "  up-db           Levanta solo las bases de datos"
    echo "  up-app          Levanta aplicación y dependencias (sin Locust)"
    echo "  up-locust       Levanta solo Locust (requiere que la app esté corriendo)"
    echo "  logs            Muestra logs de todos los servicios"
    echo "  logs-locust     Muestra logs solo de Locust"
    echo "  scale-workers N Escala los workers de Locust a N instancias"
    echo "  rebuild         Reconstruye las imágenes y levanta todo"
    echo "  status          Muestra el estado de los contenedores"
    echo "  help            Muestra esta ayuda"
}

# Función para verificar si existe .env
check_env() {
    if [ ! -f .env ]; then
        echo -e "${YELLOW}⚠️  Archivo .env no encontrado. Copia .env.example a .env y configúralo.${NC}"
        echo "cp .env.example .env"
        exit 1
    fi
}

case "$1" in
    "up")
        echo -e "${GREEN}🚀 Levantando toda la infraestructura...${NC}"
        check_env
        docker-compose up -d
        echo -e "${GREEN}✅ Infraestructura levantada!${NC}"
        echo -e "${YELLOW}📊 Locust UI disponible en: http://localhost:8089${NC}"
        echo -e "${YELLOW}🔧 Backend API disponible en: http://localhost:8080${NC}"
        ;;
    "down")
        echo -e "${RED}🛑 Bajando toda la infraestructura...${NC}"
        docker-compose down
        echo -e "${GREEN}✅ Infraestructura bajada!${NC}"
        ;;
    "up-db")
        echo -e "${GREEN}🗄️  Levantando solo bases de datos...${NC}"
        check_env
        docker-compose up -d postgres postgres-test
        echo -e "${GREEN}✅ Bases de datos levantadas!${NC}"
        ;;
    "up-app")
        echo -e "${GREEN}🔧 Levantando aplicación...${NC}"
        check_env
        docker-compose up -d postgres backend
        echo -e "${GREEN}✅ Aplicación levantada!${NC}"
        echo -e "${YELLOW}🔧 Backend API disponible en: http://localhost:8080${NC}"
        ;;
    "up-locust")
        echo -e "${GREEN}📊 Levantando Locust...${NC}"
        docker-compose up -d locust-master locust-worker
        echo -e "${GREEN}✅ Locust levantado!${NC}"
        echo -e "${YELLOW}📊 Locust UI disponible en: http://localhost:8089${NC}"
        ;;
    "logs")
        docker-compose logs -f
        ;;
    "logs-locust")
        docker-compose logs -f locust-master locust-worker
        ;;
    "scale-workers")
        if [ -z "$2" ]; then
            echo -e "${RED}❌ Especifica el número de workers: $0 scale-workers N${NC}"
            exit 1
        fi
        echo -e "${GREEN}⚖️  Escalando workers de Locust a $2 instancias...${NC}"
        docker-compose up -d --scale locust-worker=$2
        echo -e "${GREEN}✅ Workers escalados!${NC}"
        ;;
    "rebuild")
        echo -e "${GREEN}🔨 Reconstruyendo imágenes...${NC}"
        check_env
        docker-compose down
        docker-compose build --no-cache
        docker-compose up -d
        echo -e "${GREEN}✅ Infraestructura reconstruida y levantada!${NC}"
        echo -e "${YELLOW}📊 Locust UI disponible en: http://localhost:8089${NC}"
        echo -e "${YELLOW}🔧 Backend API disponible en: http://localhost:8080${NC}"
        ;;
    "status")
        echo -e "${GREEN}📋 Estado de los contenedores:${NC}"
        docker-compose ps
        ;;
    "help"|"--help"|"-h"|"")
        show_help
        ;;
    *)
        echo -e "${RED}❌ Opción no reconocida: $1${NC}"
        show_help
        exit 1
        ;;
esac 