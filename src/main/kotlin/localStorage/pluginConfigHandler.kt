package dev.niko.hypnos.localStorage

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.JButton
import javax.swing.JFileChooser
import java.awt.BorderLayout
import javax.swing.BoxLayout

class PluginConfigHandler(
    private val project: Project
) : Configurable {

    private val settings = PluginSettings.getInstance(project)
    
    private val modItemsFileField = JTextField()
    private val modelDirField = JTextField()
    private val javModBlockField = JTextField()

    override fun getDisplayName(): String = "Item Generator"

    override fun createComponent(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        fun addPathRow(
            label: String,
            field: JTextField,
            directory: Boolean
        ) {
            val row = JPanel(BorderLayout(8, 0))
            row.add(JLabel(label), BorderLayout.WEST)
            row.add(field, BorderLayout.CENTER)

            val browseButton = JButton("Browse...")
            browseButton.addActionListener {
                val chooser = JFileChooser()
                chooser.fileSelectionMode =
                    if (directory) JFileChooser.DIRECTORIES_ONLY
                    else JFileChooser.FILES_ONLY

                val result = chooser.showOpenDialog(panel)

                if (result == JFileChooser.APPROVE_OPTION) {
                    field.text = chooser.selectedFile.absolutePath
                }
            }

            row.add(browseButton, BorderLayout.EAST)
            panel.add(row)
        }
        
        addPathRow("ModItems Java:", modItemsFileField, false)
        addPathRow("Resource directory:", modelDirField, true)
        addPathRow("Java ModBlock File:", javModBlockField, false)

        reset()
        return panel
    }

    override fun isModified(): Boolean {
        val state = settings.getState()

        return modItemsFileField.text != state.javItemFile ||
                modelDirField.text != state.javResourceFile ||
                javModBlockField.text != state.javModBlockFile
    }

    override fun apply() {
        val state = settings.getState()

        state.javItemFile = modItemsFileField.text.trim()
        state.javResourceFile = modelDirField.text.trim()
        state.javModBlockFile = javModBlockField.text.trim()
    }

    override fun reset() {
        val state = settings.getState()


        modItemsFileField.text = state.javItemFile
        modelDirField.text = state.javResourceFile
        javModBlockField.text = state.javModBlockFile
    }
}
