package ru.health.stream.buildlogic.extension

import com.android.build.api.dsl.AndroidResources
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.BuildFeatures
import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.DefaultConfig
import com.android.build.api.dsl.Installation
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.ProductFlavor
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.the

private typealias AndroidExtensions = CommonExtension<
        out BuildFeatures,
        out BuildType,
        out DefaultConfig,
        out ProductFlavor,
        out AndroidResources,
        out Installation>

private val Project.javaExtension: JavaPluginExtension
    get() = extensions.findByType(JavaPluginExtension::class)
        ?: error("This is not java library")

private val Project.androidExtension: AndroidExtensions
    get() = extensions.findByType(BaseAppModuleExtension::class)
        ?: extensions.findByType(LibraryExtension::class)
        ?: error("This is not android library or android application")

private val Project.applicationExtension: ApplicationExtension
    get() = extensions.findByType(ApplicationExtension::class)
        ?: error("This is not android application")

val Project.libs
    get() = the<LibrariesForLibs>()

val Project.projectJavaVersion: JavaVersion
    get() = JavaVersion.toVersion(libs.versions.java.get().toInt())

fun Project.java(block: JavaPluginExtension.() -> Unit): Unit = block(javaExtension)
fun Project.android(block: AndroidExtensions.() -> Unit): Unit = block(androidExtension)
fun Project.application(block: ApplicationExtension.() -> Unit): Unit = block(applicationExtension)
