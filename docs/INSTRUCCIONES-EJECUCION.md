# Cayo Cartagena — instrucciones de ejecución

## Requisitos
- JDK 17 o superior (el proyecto se compila con `--release 17`).
- MySQL 8 (también probado con MariaDB 10.4).
- No hace falta instalar Maven: se usa el wrapper `.\mvnw.cmd`.

## 1. Crear la base de datos
Ejecutar **una sola vez**, sobre una base vacía, todo el contenido de
`database/cayo_cartagena.sql` en MySQL Workbench o con el cliente de línea de
comandos:

```powershell
Get-Content database\cayo_cartagena.sql | mysql -u root -p
```

El script crea la base `CayoCartagena` con **nueve tablas**:

| Tabla | Contenido |
| --- | --- |
| `Usuario` | Cuenta y rol: Cliente, Proveedor, Agencia o Supervisor |
| `Proveedor` | Datos del negocio (solo usuarios Proveedor) |
| `Turista` | Tipo de turista y nacionalidad (solo usuarios Cliente) |
| `Agencia` | Razón social, NIT y RNT (solo usuarios Agencia) |
| `Oferta` | Servicio publicado, precio, cupos, días y horario |
| `Oferta_imagen` | Imágenes adjuntas a una oferta |
| `Itinerario` | Compra en curso o reservada de un cliente |
| `Detalle_Itinerario` | Ofertas que componen el itinerario |
| `Reserva` | Pago simulado asociado al itinerario |

Al final del script se crea un usuario **supervisor** de prueba:

- Correo: `supervisor@cayocartagena.com`
- Contraseña: `Supervisor123`

La contraseña se guarda tal cual en la base de datos (texto plano). Es una
simplificación del prototipo para poder revisarla y cambiarla a mano; los demás
usuarios se crean desde la interfaz.

## 2. Configurar la conexión
La conexión vive en `src/main/resources/application-mysql.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/CayoCartagena?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota
spring.datasource.username=root
spring.datasource.password=
```

Por defecto usa `root` sin contraseña, que es lo habitual en XAMPP. Si tu MySQL
tiene otra clave, edita ese archivo **solo en tu PC** o define las variables de
entorno antes de arrancar:

```powershell
$env:DB_USERNAME = "tu_usuario"
$env:DB_PASSWORD = "tu_contraseña"
```

También se puede cambiar la URL completa con `DB_URL`. El perfil `mysql` ya está
activo en `application.properties`; no hay que pasar ningún flag adicional.

## 3. Arrancar
```powershell
.\mvnw.cmd spring-boot:run
```

O sobre el JAR ya generado:

```powershell
.\mvnw.cmd clean package
java -jar target\proaula-turismo-0.0.1-SNAPSHOT.jar
```

La aplicación queda en `http://localhost:8080`.

## 4. Flujo completo
1. El turista, proveedor o agencia se registra desde la pantalla de registro.
2. Proveedor o agencia publica una oferta; queda en estado **Pendiente**.
3. El supervisor la aprueba o rechaza en `/supervisor/lista`.
4. Solo las ofertas **Publicada** aparecen en `/catalogo`, donde se pueden buscar
   por nombre, categoría, isla o descripción.
5. El turista agrega ofertas al carrito y pulsa **Pagar itinerario**.
6. El sistema elige automáticamente la primera fecha disponible: busca desde
   mañana y hasta 90 días adelante el primer día en que **todas** las ofertas del
   carrito coinciden con sus días disponibles. Si no existe, muestra un error y
   no deja continuar.
7. Se elige el método de pago (`Tarjeta de crédito`, `PSE` o `Efectivo`) y se
   confirma. El pago es **simulado**: no se conecta a ninguna pasarela real.
8. Al aprobar el pago se descuentan los cupos, se guarda la reserva y se muestra
   la factura imprimible.

## 5. Reglas del prototipo
- Categorías válidas: Alquiler de carpas y cabañas, Almuerzo, Hospedaje,
  Actividades, Pasadía, Paquete y Transporte.
- Islas válidas: Islas del Rosario, Barú y Tierra Bomba.
- La tarifa debe ser mayor que cero, con máximo dos decimales. Los cupos deben
  estar entre 1 y 100 000.
- Si una oferta tiene días u horario, se validan contra las listas permitidas y
  la hora de fin debe ser posterior a la de inicio.
- Editar una oferta la devuelve a **Pendiente** para una nueva revisión.
- Eliminar una oferta que ya forma parte de un itinerario no la borra: la marca
  como **Cancelada** para no romper las reservas existentes.
- Los cupos son **globales por oferta**, no por fecha. Es una simplificación del
  prototipo: si una oferta tiene 10 cupos, se descuentan del total al pagar,
  aunque la fecha elegida sea otra.
- La contraseña debe tener entre 8 y 72 caracteres.

## 6. Seguridad mínima
Este es un prototipo académico y **no** usa Spring Security. La protección es la
mínima para que el flujo funcione:

- Control de acceso por rol con un interceptor propio (`/proveedor/`,
  `/agencia/`, `/supervisor/` y `/turista/`).
- Contraseñas en texto plano para que el prototipo sea fácil de revisar. En un
  entorno real habría que guardar un hash; esa decisión se dejó fuera a propósito.
- Sesión en `HttpSession` con cookie `HttpOnly` y `SameSite=Lax`.
- Cabeceras `X-Content-Type-Options: nosniff` y `X-Frame-Options: SAMEORIGIN`.
- Página de error genérica, sin trazas ni mensajes internos.

No se implementaron CSRF, bloqueo por intentos fallidos, rate limiting ni
recuperación de contraseña. Para un entorno real habría que agregarlos.

## Notas
- Los archivos subidos (RNT e imágenes de ofertas) se guardan en `uploads/`.
  La ruta se puede cambiar con la propiedad `app.upload.dir`.
- Al usar MariaDB, Hibernate puede mostrar un aviso sobre "versión 5.5.5"; no
  afecta el funcionamiento.
- `docs/diagramas/` contiene los diagramas del proyecto.
