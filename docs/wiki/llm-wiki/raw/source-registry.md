# Registro de fuentes

**Propósito:** inventario verificable de las fuentes curadas. Las rutas son
relativas a la raíz del workspace. Los hashes corresponden al 2026-09-16.

| ID | Fuente canónica | Uso | SHA-256 |
|---|---|---|---|
| SRC-README | `README.md` | alcance, stack y arranque | `9d74ad57a2536804a85fd6c75077c851a75305a0ba7c944cce6da5f6fba9554a` |
| SRC-PRD | `PRD.md` | requisitos funcionales y reglas de negocio | `bf7a128902dc35a5e38b3a88c7a561b89d7c15cd0831f1f903b6d70527f649e7` |
| SRC-TECH | `RESTRICCIONES_TECNICAS.md` | arquitectura y restricciones técnicas | `8601b6b2e0f8da85775db9a225bc68f6642e86ee47e841e4d04b53533d021efb` |
| SRC-SESSIONS | `GUIA_SESIONES_S2_S6.md` | entregables y trazabilidad de sesiones | `f40b389fd4515cd1e4560e73bf123858da20cec7b1fd2f0fd8e10a410942b891` |
| SRC-3FN | `database/REQUISITOS_NORMALIZACION_3FN.md` | criterios académicos de normalización | `6d3dfa8fe050712fb46be081d542623748954cf8929a262b9f96c6bfdd20dbac` |
| SRC-SCHEMA | `database/reference/db.sql` | esquema MySQL ejecutable | `f59b73090220c9e76257633a7918c468f093cf74b90204f9d9cf12f0055c16bb` |
| SRC-NORMALIZATION | `database/reference/NORMALIZACION_3FN.md` | dependencias funcionales y justificación | `424960990f9a194954220f2388a26e621171751ee1e98250c0952baca1827442` |
| SRC-ERD | `database/reference/erd.mmd` | relaciones y cardinalidades | `f6664b681c604cd64b2eca2459b517b428c1575f97f7f7d24f142714c5e5dc6b` |
| SRC-COMPOSE | `docker-compose.yml` | topología y arranque de infraestructura | `29771ea9116d86c59e94929506590e28403cf2453cef5ee43601c2642126bcb4` |
| SRC-API-S2 | `citas-api/pom.xml` | dependencias y runtime del incremento S2 | `f85106897a92583a20fe84ca538a98d68d52b4553c4b55e713ccafb8becdd987` |
| SRC-MIGRATION-S2 | `citas-api/src/main/resources/db/migration/V1__auth_bootstrap.sql` | bootstrap de autenticación Flyway | `779ab0bc9120ea0d8e509533a83d6e07bf20ffa97d59517962bd34b58c161c9b` |
| SRC-WEB-S2 | `citas-web/package.json` | stack y scripts del frontend S2 | `eccd2b8374e537367a400fcc4523b156e84b57450e5144ba144c106db0f78370` |

## Regla de actualización

Si una fuente canónica cambia mediante una decisión aprobada, conserva esta
entrada como historial en el log, actualiza su hash y realiza una nueva ingesta.
No edites una fuente para hacerla coincidir con una página wiki.
