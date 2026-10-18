package dev.niko.hypnos.globals
import com.intellij.openapi.project.Project
import dev.niko.hypnos.localStorage.PluginSettings
import dev.niko.hypnos.utils.pullDir

class Globals(private val project: Project) {

    val settings = PluginSettings.getInstance(project)
    object markers {
        const val marker = "//Marker"
    }


    val paths = Paths()
    inner class Paths{
        val javModBlockFile = pullDir(settings.getState().javModBlockFile, project) ?: error("Missing javModBlockFile")
        val langFile = pullDir(settings.getState().javResourceFile + """\lang\en_us.json""", project) ?: error("Could not find en_us.json in the project")
        val javItemFile = pullDir(settings.getState().javItemFile, project) ?: error("Could not find javItemFile")
        val itemModelDir = pullDir(settings.getState().javResourceFile + """\models\item""", project) ?: error("Could not find model directory")
        val blockModelDir = pullDir(settings.getState().javResourceFile + """\models\block""", project) ?: error("Could not find model directory")
        val blockStateDir = pullDir(settings.getState().javResourceFile + """\blockstates""", project) ?: error("Could not find blockState directory")
    }

    val contents = Contents()
    inner class Contents(){
        val langContent = paths.langFile.readText().trim()
        val javItemContent = paths.javItemFile.readText()
        val javModBlockContent = paths.javModBlockFile.readText()
    }
}