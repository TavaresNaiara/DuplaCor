# ==============================================================================
# Etapa 1: Build da Aplicação Java com Maven (Multi-stage build)
# ==============================================================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Copia pom.xml primeiro para otimizar cache de dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte
COPY src ./src

# Executa o build e gera o Fat JAR executável
RUN mvn clean package -DskipTests

# ==============================================================================
# Etapa 2: Imagem Final de Execução (Runtime leve)
# ==============================================================================
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Cria usuário não-root para segurança
RUN addgroup -S duplacor && adduser -S duplacor -G duplacor

# Copia o artefato construído da etapa de build e a pasta web
COPY --from=builder /build/target/duplacor-1.0.0.jar app.jar
COPY web ./web

# Ajusta permissões
RUN chown -R duplacor:duplacor /app
USER duplacor

# Variáveis de ambiente padrão
ENV DB_HOST=mysql \
    DB_PORT=3306 \
    DB_NAME=duplacor \
    DB_USER=duplacor \
    DB_PASSWORD=duplacor \
    PORT=8080

EXPOSE 8080

# Execução da aplicação Web
ENTRYPOINT ["java", "-jar", "app.jar"]
