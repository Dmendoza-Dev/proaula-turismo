# Cayo Cartagena — instrucciones de ejecución

## Requisitos
- JDK 17 o superior (se compila con `--release 17`).
- MySQL 8 (también probado con MariaDB 10.4).
- No es necesario instalar Maven: se usa `.\mvnw.cmd`.

## 1. Crear la base de datos
Ejecutar **todo** el contenido de `database/cayo_cartagena.sql` en MySQL Workbench
(o con el cliente de línea de comandos). El script crea la base `CayoCartagena`,
sus ocho tablas y la tabla de imágenes de ofertas.

## 2. Configurar la conexión
Editar `src/main/resources/application-mysql.properties` **solo en tu PC**:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/CayoCartagena?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_CONTRASEÑA
```

También se pueden pasar variables de entorno `DB_URL`, `DB_USERNAME` y
`DB_PASSWORD` sin tocar el archivo. **No modificar `application.properties`**:
ese archivo no trae datasource a propósito.

## 3. Arrancar
El perfil `mysql` ya está activo en `application.properties`, así que basta con:

```powershell
.\mvnw.cmd spring-boot:run
```

O, sobre el JAR ya generado:

```powershell
java -jar target\proaula-turismo-0.0.1-SNAPSHOT.jar
```

Si se quiere forzar el perfil de forma explícita, también funciona:
`.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql`.

La aplicación queda en `http://localhost:8080`.

## 4. Usuarios para probar
Crear desde la propia interfaz:
- Turista: `/registro-turista`
- Proveedor: `/registro?tipo=proveedor`
- Agencia: `/registro-agencia`

No hay pantalla de registro para supervisor. Si se necesita uno para probar la
auditoría, ejecutar este SQL (contraseña: `Supervisor123`; cámbiala después):

```sql
USE CayoCartagena;
INSERT INTO Usuario (Nombres, Apellidos, Documento, Correo, Telefono, Contrasena, Tipo_usuario)
VALUES ('Supervisor', 'Operativo', '900001', 'supervisor@cayocartagena.com', '3000000001',
        '120000:LsLluSQIQoKMWfg34LDLVQ==:M2gNhpugjRLJv4rMgLfWK4UOBk2FslKXCiVFkcmyJX4=',
        'Supervisor');
```

El hash es PBKDF2-HMAC-SHA256 (120 000 iteraciones); la aplicación nunca guarda
contraseñas en texto plano.

## 5. Flujo completo
1. Proveedor o agencia publica una oferta: queda en estado **Pendiente**.
2. Supervisor aprueba o rechaza en `/supervisor/lista`.
3. Solo las ofertas **Publicada** aparecen en `/catalogo`.
4. El turista agrega servicios al carrito flotante y pulsa **Pagar itinerario**.
5. Se crea el itinerario, se elige el método de pago y se confirma.
6. Se guarda la reserva y se muestra la factura imprimible.

El pago es **simulado**: no se conecta a ninguna pasarela real.

## Notas
- Los archivos subidos (RNT, imágenes de ofertas) se guardan en la carpeta
  `uploads/` del proyecto. Se puede cambiar con la propiedad `app.upload.dir`.
- Al usar MariaDB, Hibernate puede mostrar un aviso sobre "versión 5.5.5";
  no afecta el funcionamiento.
- `docs/diagramas/` contiene los diagramas del proyecto.
