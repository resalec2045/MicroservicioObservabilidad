# API Gateway

Microservicio simple que actúa como API Gateway para enrutar operaciones hacia el servicio de autenticación y el servicio de perfil.

Rutas principales:

- POST /api/auth/login -> proxy a AUTH_URL/api/users/login
- POST /api/auth/register -> proxy a AUTH_URL/api/users
- DELETE /api/auth/users/:id -> proxy a AUTH_URL/api/users/:id y notifica (si NOTIF_URL está configurada)
- GET /api/users/combined/:userId -> obtiene datos de seguridad + perfil y devuelve una respuesta unificada
- PUT /api/users/combined/:userId -> acepta body { auth?, profile? } y envía las partes a los servicios correspondientes

Variables de entorno (archivo .env o sistema):

- AUTH_URL (por defecto http://localhost:8080)
- PROFILE_URL (por defecto http://localhost:8081)
- NOTIF_URL (opcional) — si se define, el gateway intentará POST /api/notifications tras una eliminación
- PORT (por defecto 8090)

Cómo arrancar (local):

1. En la carpeta `api-gateway` ejecutar `npm install`.
2. Definir variables de entorno o copiar `.env.example` y editar.
3. `npm start`.

Notas:

- Este gateway es deliberadamente simple y realiza las operaciones en modo síncrono. Para producción se recomendaría usar un API Gateway dedicado (Kong, Traefik, Nginx, AWS API Gateway) o un proxy más completo y añadir circuit breakers, timeouts, retries, autenticación y observabilidad.
