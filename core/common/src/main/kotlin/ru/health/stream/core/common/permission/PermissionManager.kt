package ru.health.stream.core.common.permission

interface PermissionManager {

    suspend fun request(permission: Permission): PermissionStatus
    suspend fun requestGroup(vararg permissions: Permission): Map<Permission, PermissionStatus>

    suspend fun check(permission: Permission): PermissionStatus
    suspend fun checkGroup(vararg permissions: Permission): Map<Permission, PermissionStatus>
}
