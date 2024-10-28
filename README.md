# Weather API

Esta es una API para obtener datos del clima en tiempo cacheados en Redis. La API permite consultar el clima para ubicaciones específicas.

## Requisitos Previos

- JDK 21
- Gradle
- Redis Cloud
- Una clave de API para el servicio de clima (Tomorrow.io)

## Instalación

1. Clona este repositorio:

   ```bash
   git clone https://github.com/lroca7/weather-wird
   cd ktor-sample-wird
2. Configura las credenciales
    ```bash
        ktor:
          application:
            modules:
             - com.example.ApplicationKt.module
            apiKey: "TU_API_KEY_AQUI"
            redis:
              url: "REDIS_URL"
              password: "REDIS_PASSWORD"
            deployment:
              port: 5000

3. Ejecuta el proyecto:

   ```bash
   ./gradlew build

   ./gradlew run
   
4. Consultar clima de una locacion

   ```bash
    http://127.0.0.1:5000/weather/USA
   ```
   Retornara un JSON con los datos cacheados en redis relacionados con el clima de la locacion

5. 