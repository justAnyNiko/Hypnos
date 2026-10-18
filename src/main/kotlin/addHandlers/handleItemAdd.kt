package dev.niko.hypnos.addHandlers


import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import dev.niko.hypnos.globals.Globals.markers.marker
import java.io.File
import dev.niko.hypnos.globals.Globals
import dev.niko.hypnos.utils.reloadFile


class GenerateItemAction : AnAction() {



    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val globals = Globals(project)




        try {
            val root = File(project.basePath ?: return)


            if (!globals.paths.itemModelDir.exists() && !globals.paths.itemModelDir.mkdirs()) {
                error("Could not create model directory: ${globals.paths.itemModelDir.absolutePath}")
            }


            // Ask for item ID
            val input = Messages.showInputDialog(
                project,
                "Enter the item ID (e.g. copper_ingot):",
                "Generate Mod Item",
                Messages.getQuestionIcon()
            ) ?: return

            val itemId = input.trim()
                .lowercase()
                .replace(" ", "_")
                .replace(" ", "_")

            require(itemId.matches(Regex("[a-z0-9_]+"))) {
                "Item ID must contain only lowercase letters, numbers, and underscores"
            }

            require(itemId.isNotEmpty()) {
                "Item ID cannot be empty"
            }

            val displayName = itemId
                .split("_")
                .joinToString(" ") {
                    it.replaceFirstChar { c -> c.uppercase() }
                }

            // Prepare language entry
            val langEntry =
                "\"item.createindustrial.$itemId\": \"$displayName\""

            require(!globals.contents.langContent.contains("\"item.createindustrial.$itemId\"")) {
                "This item already exists in en_us.json"
            }

            require(globals.contents.langContent.startsWith("{") && globals.contents.langContent.endsWith("}")) {
                "The language file does not look like a JSON object"
            }

            val innerJson = globals.contents.langContent
                .removePrefix("{")
                .removeSuffix("}")
                .trim()

            val updatedLang = if (innerJson.isEmpty()) {
                "{\n  $langEntry\n}"
            } else {
                "{\n  $innerJson,\n  $langEntry\n}"
            }


            check(marker in globals.contents.javItemContent) {
                "Could not find register() in ModItems.java"
            }

            check(!globals.contents.javItemContent.contains("ITEMS.registerSimpleItem(\"$itemId\"")) {
                "This item appears to already be registered in ModItems.java"
            }

            val javItemEntry = """
                public static final DeferredItem<Item> ${itemId.uppercase()} =
                    ITEMS.registerSimpleItem("$itemId");
            """.trimIndent()

            val updatedJava = globals.contents.javItemContent.replaceFirst(
                marker,
                "$javItemEntry\n\n    $marker"
            )

            // Prepare model JSON
            val modelFile = File(globals.paths.itemModelDir, "$itemId.json")

            check(!modelFile.exists()) {
                "Model file already exists: ${modelFile.name}"
            }

            val modelEntry = """
                {
                  "parent": "minecraft:item/generated",
                  "textures": {
                    "layer0": "createindustrial:item/$itemId"
                  }
                }
            """.trimIndent()

            // Write changes
            globals.paths.langFile.writeText(updatedLang)
            modelFile.writeText(modelEntry)
            globals.paths.javItemFile.writeText(updatedJava)

            Messages.showInfoMessage(
                project,
                "Created item: $itemId\n" +
                        "Display name: $displayName\n" +
                        "Java file: ${globals.paths.javItemFile.absolutePath}\n" +
                        "Model file: ${modelFile.absolutePath}\n\n" +
                        "Remember to add the matching PNG texture!",
                "Item Generator"
            )

            reloadFile(globals.paths.langFile)
            reloadFile(globals.paths.javItemFile)

        } catch (e: Exception) {
            Messages.showErrorDialog(
                project,
                "${e.javaClass.simpleName}: ${e.message}",
                "Item Generator Error"
            )
            e.printStackTrace()
        }
    }
}
