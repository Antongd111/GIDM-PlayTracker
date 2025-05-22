# PlayTracker

PlayTracker es una aplicación desarrollada en Kotlin que permite a los usuarios gestionar su biblioteca de videojuegos, permitiendo añadir los videojuegos a la biblioteca para llevar un seguimiento de los mismos, buscar información de ellos, encontrar videojuegos relacionados y ver bibliotecas de amigos, entre otras funciones.

Se implementará un sistema de recomendación de videojuegos basado en los que ha jugado el usuario, permitiendo descubrir nuevos juegos que podrían interesar.

La aplicación utiliza la api de RAWG, que contiene información actualizada de todos los videojuegos disponibles en todas las plataformas.

## Instalación

### 1. Clona el repositorio

```bash
git clone https://github.com/Antongd111/GIDM-PlayTracker.git
```

### 2. Crea el entorno virtual (venv)

```bash
cd backend
python -m venv venv
venv\Scripts\activate  # En Windows
```

### 3. Instala las dependencias
```bash
pip install -r requirements.txt
```

### 4. Crea el archivo con las variables de entorno (.env)
En la raíz del backend, crea un archivo .env con las siguientes variables:
```bash
SECRET_KEY=
ACCESS_TOKEN_EXPIRE_MINUTES=60
ALGORITHM=HS256
DATABASE_URL=postgresql+asyncpg://playtracker:playtracker@localhost:5432/playtracker
```

### 5. Crea el contenedor con la base de datos
Asegúrate de tener el demonio de Docker corriendo en el sistema, y ejecuta en la raíz del backend:
```bash
docker-compose up -d
```

Esto inicia un contenedor con la base de datos, con los siguientes parámetros:
- Usuario: playtracker
- Contraseña: playtracker
- Base de datos: playtracker
- Puerto: 5432

### 6. Ejecutar el servidor en desarrollo
```bash
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```