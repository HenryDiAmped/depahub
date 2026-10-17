# Informe de pruebas unitarias — DepaHub (backend)

Este documento describe **únicamente las pruebas del backend** (`depahub`). El frontend (`depahub-frontend`) todavía no tiene suite de pruebas.

## Software utilizado

| Herramienta | Uso |
|---|---|
| **Java 21** | Lenguaje y runtime de las pruebas |
| **Apache Maven** (`mvnw`) | Compilación y ejecución del suite |
| **JUnit 5** (Jupiter) | Framework de pruebas unitarias (`@Test`, aserciones) |
| **Mockito** | Simulación de repositorios, servicios y cadena de filtros (`@Mock`, `when`, `verify`) |
| **Spring Boot 4.1.0** (`spring-boot-starter-webmvc-test`, `security-test`, `data-jpa-test`) | Trae JUnit y Mockito; `MockHttpServletRequest`/`Response` en el filtro JWT |
| **BCrypt** (`BCryptPasswordEncoder`) | Verificación del hash de contraseñas en `AdministradorService` |
| **JJWT 0.12.6** | Generación y validación de tokens en `JwtService` |
| **OpenPDF 2.0.3** | Lectura del PDF generado (`PdfReader`, `PdfTextExtractor`) |

Las pruebas unitarias **no usan MySQL**. Los repositorios se reemplazan con mocks. La única prueba de integración es `DepahubApplicationTests` (`@SpringBootTest`), que comprueba que el contexto Spring arranca.

Cómo ejecutarlas desde `depahub/`:

```bash
./mvnw test
```

En Windows:

```bash
.\mvnw.cmd test
```

---

## Cantidad de pruebas

| Concepto | Cantidad |
|---|---|
| Clases de test | 14 |
| Casos de prueba (`@Test`) | **69** |
| Pruebas unitarias (JUnit + Mockito) | 68 |
| Prueba de integración (contexto Spring) | 1 |

### Detalle por clase

| Clase de test | Casos | Tipo |
|---|---:|---|
| `AdministradorServiceTest` | 11 | Unitaria |
| `ContratoServiceTest` | 17 | Unitaria |
| `CuentaServiceTest` | 8 | Unitaria |
| `BalanceMensualServiceTest` | 7 | Unitaria |
| `BaseCrudServiceTest` | 7 | Unitaria |
| `JwtAuthenticationFilterTest` | 6 | Unitaria |
| `JwtServiceTest` | 5 | Unitaria |
| `ContratoPdfServiceTest` | 2 | Unitaria |
| `IngresoServiceTest` | 1 | Unitaria |
| `EgresoServiceTest` | 1 | Unitaria |
| `InquilinoServiceTest` | 1 | Unitaria |
| `InmuebleServiceTest` | 1 | Unitaria |
| `PropiedadServiceTest` | 1 | Unitaria |
| `DepahubApplicationTests` | 1 | Integración |
| **Total** | **69** | |

---

## Funciones / comportamientos probados

### `AdministradorService`

- Cifrar la contraseña al crear
- Login contra hash BCrypt
- Login con email inexistente (401)
- Login con password incorrecta (401)
- Crear sin password (400)
- Crear con email duplicado (409)
- Actualizar conservando la password si viene vacía
- Actualizar recifrando si viene una password nueva
- Actualizar conservando el mismo email (sin 409)
- Migrar contraseñas en texto plano
- No migrar hashes que ya son BCrypt

### `CuentaService`

- Permitir cuenta sin fecha de vencimiento
- Rechazar vencimiento anterior a la emisión
- Permitir vencimiento igual a la emisión
- Al saldar: crear ingreso/egreso y actualizar totales del balance
- No crear movimiento si la cuenta sigue pendiente
- No duplicar movimiento si ya estaba saldada
- Listar por administrador y por inquilino

### `ContratoService`

- Cálculo de cuotas (mensual, bimestral, frecuencia 3, prorrateo)
- Validaciones: frecuencia no positiva, fecha fin anterior, duración menor a un mes, frecuencia mayor al rango, fechas nulas
- Crear garantía y cuotas con vencimientos
- Cuota prorrateada con importe proporcional
- Crear balance del mes si el contrato está vigente
- No crear balance si el contrato no está vigente
- Acceso del dueño vs administrador ajeno (403)
- Listar por administrador y por inquilino

### `ContratoPdfService`

- Generar un PDF con datos de arrendador, arrendatario, inmueble, ubicación y condiciones
- Generar el PDF aunque no haya condiciones particulares

### `JwtService`

- Generar y leer un token firmado
- Rechazar token alterado
- Rechazar secret de menos de 32 caracteres
- Rechazar expiración menor o igual a cero
- Rechazar token expirado

### `JwtAuthenticationFilter`

- Continuar sin autenticar si no hay header o no es `Bearer`
- Autenticar con el email del token válido
- No pisar una autenticación ya existente
- Responder 401 si el token es inválido o el argumento es ilegal

### `BalanceMensualService`

- Listar por administrador
- Devolver el balance existente sin crear otro
- Crear un balance en cero si no existe para el período
- Recalcular utilidad al guardar totales automáticos
- Completar o conservar `fechaGeneracion` al crear y al actualizar

### `BaseCrudService`

- Listar, obtener por id, 404 si no existe
- Crear (preparar + guardar)
- Actualizar (asignar id + preparar)
- Eliminar solo si el registro existe

### CRUD de consulta

- `IngresoService.listarPorBalance`
- `EgresoService.listarPorBalance`
- `InquilinoService.listarPorInmueble`
- `InmuebleService.listarPorPropiedad`
- `PropiedadService.listarPorAdministrador`

### Arranque de la aplicación

- Carga del contexto Spring (`contextLoads`)

---

## Alcance

Se probaron **servicios y el filtro JWT** del backend. No se incluyen pruebas de controladores HTTP (`AuthController`), ni de repositorios contra MySQL (`@DataJpaTest`).
