package dev.niko.hypnos.utils

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

fun reloadFile(file: File): VirtualFile? {
    val localFileSystem = LocalFileSystem.getInstance()

    file.parentFile?.let { parent ->
        localFileSystem.refreshAndFindFileByIoFile(parent)?.refresh(false, false)
    }

    return localFileSystem.refreshAndFindFileByIoFile(file)
}
fun pullDir(fileName: String, project: Project): File? {
    if (fileName.isEmpty()) {
        Messages.showErrorDialog(project, "Empty ${fileName}", "Error")
    }
    if (!File(fileName).exists()) {
        Messages.showErrorDialog(project, "File does not exist ${fileName}", "Error")
        return null
    }
    return File(fileName)
}

