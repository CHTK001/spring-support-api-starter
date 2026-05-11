# Module Split Plan

## Goal

- Keep `spring-api-support-common-project-starter` as the base project module.
- Move AI capability implementation into `spring-api-support-common-project-ai-starter`.
- Move device database resources into `spring-api-support-common-project-device-starter`.
- Keep `device -> project` and `ai -> project`.
- Keep existing package names unchanged for fast migration.

## Responsibility Boundary

### `spring-api-support-common-project-starter`

- `SysProject` and project base configuration
- `SysSecret`
- project default mapping
- email / sms template and log capability
- shared project auto-configuration
- no device / ai implementation or schema ownership

### `spring-api-support-common-project-ai-starter`

- AI controllers
- AI services and implementations
- AI mappers and mapper XML
- AI request / response pojos
- AI tests
- AI database init resources

### `spring-api-support-common-project-device-starter`

- device entities, channels, orgs, events
- vendor device capability
- device merchant access based on `SysProject`
- device database init resources

## Migration Scope

### Move From `common-project` To `common-ai`

- AI controllers, services, impls, mappers, mapper XML, pojos, tests
- AI database init resources

### Move From `common-project` To `device`

- `sys_device`
- `sys_device_channel`
- `sys_device_org`
- `sys_device_camera_record`
- `sys_device_card_event`
- `sys_device_message_event`

## Execution Strategy

1. Add the AI module and wire reactor/pom dependencies.
2. Move AI source files first while keeping package names unchanged.
3. Move AI SQL resources into the AI module and remove them from project resources.
4. Move device SQL resources into the device module and remove them from project resources.
5. Make `device -> project` explicit at pom level.
6. Compile `common-project`, `common-ai`, and `device` together.
7. Fix broken imports or dependency leaks.

## Risks

- SQL split must avoid duplicate `sys_ai_*` and `sys_device*` table creation across modules.
- Some AI services rely on `SysProjectService`; this should remain through `ai -> project`.
- Device code still uses `SysProject` as merchant/config carrier; this should remain through `device -> project`.

## Validation

- `mvn -pl spring-api-support-common-project-starter,spring-api-support-common-project-ai-starter,spring-api-support-common-project-device-starter -DskipTests compile`
