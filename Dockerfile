# -------- BUILD STAGE --------
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q clean package


# -------- RUN STAGE --------
FROM tomcat:9.0-jdk21-temurin

# Eliminem apps per defecte
RUN rm -rf /usr/local/tomcat/webapps/*

# Copiem el WAR generat com a ROOT
COPY --from=build /app/target/AT_Lab.war \
    /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
