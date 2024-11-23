import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class CompileTask
@Inject constructor(objectFactory: ObjectFactory,
                    @Internal
                    val execOperations: ExecOperations) : DefaultTask() {

    @get:Input
    abstract val moduleName: Property<String>

    @get:InputFiles
    val sources = objectFactory.fileCollection().from(moduleName.map {
        listOf(it, "lib2", if (it == "a") "lib1" else "lib3")
    })

    @get:OutputFiles
    val outputs = objectFactory.fileCollection().from(moduleName.map {
        listOf("out/$it/${it}Kt.class", "out/lib2/lib2.class", if (it == "a") "out/lib1/lib1.class" else "out/lib3/lib3.class")
    })

    val libs
        @Internal get() = sources.filter { it.name.startsWith("lib") }.map { it.name }

    val mName
        @Internal get() = moduleName.get()

    @TaskAction
    fun compile() {
        //        println(moduleName.get())
        println(sources.files)
        println(outputs.files)
        for (lib in libs)
            execOperations.exec {
                println("compiling $lib...")
                commandLine("kotlinc", "$lib/$lib.kt", "-d", "out/$lib")
            }
        execOperations.exec {
            println("compiling program $mName...")
            commandLine("kotlinc", "$mName/$mName.kt", "-cp", libs.joinToString(":") { "out/$it" }, "-d", "out/$mName")
        }
    }
}