
import java.net.URLClassLoader
import java.lang.ClassLoader

plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

tasks.register("printIc2API") {
    doLast {
        val urls = project.configurations.flatMap { config ->
            if (config.isCanBeResolved) {
                try {
                    config.resolve().map { it.toURI().toURL() }
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }.distinct().toTypedArray()
        val classLoader = URLClassLoader(urls, ClassLoader.getSystemClassLoader())
        try {
            val clazz = classLoader.loadClass("ic2.api.item.IC2Items")
            println("Fields in IC2Items:")
            clazz.fields.forEach { field ->
                try {
                    println("  ${field.name}: ${field.get(null)}")
                } catch (ex: Exception) {
                    println("  ${field.name}: (error: ${ex.message})")
                }
            }
            println("Methods in IC2Items:")
            clazz.methods.forEach { method -> println("  $method") }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:unchecked")
    options.compilerArgs.add("-Xlint:deprecation")
}
