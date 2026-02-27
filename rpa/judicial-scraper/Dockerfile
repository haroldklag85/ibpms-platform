# Usamos una imagen Slim para ocupar el menor espacio posible en RAM y Disco
FROM python:3.11-slim

# Metadatos
LABEL maintainer="iBPMS Architecture Team"
LABEL description="Contenedor Efímero para Web Scraping Judicial - V1"

# Establecer directorio de trabajo
WORKDIR /app

# Copiar dependencias primero para aprovechar la caché de capas de Docker
COPY requirements.txt .

# Instalar dependencias sin guardar el caché de pip para reducir aún más el tamaño de la imagen
RUN pip install --no-cache-dir -r requirements.txt

# Copiar el script
COPY scraper.py .

# Crear un usuario no root por motivos de seguridad (Zero Trust)
RUN useradd -m rpauser
USER rpauser

# El comando por defecto, que iniciará el scraper y, gracias al diseño del script,
# morirá automáticamente luego de enviar el webhook.
CMD ["python", "scraper.py"]
