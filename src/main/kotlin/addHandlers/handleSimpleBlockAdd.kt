package dev.niko.hypnos.addHandlers

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import dev.niko.hypnos.globals.Globals
import java.io.File
import dev.niko.hypnos.localStorage.PluginSettings
import dev.niko.hypnos.utils.reloadFile
import dev.niko.hypnos.utils.pullDir
import dev.niko.hypnos.globals.Globals.markers.marker
import dev.niko.hypnos.globals.Globals.Paths


class GenerateSimpleBlockAction : AnAction() {



    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val globals = Globals(project)


        try {

            if (!globals.paths.itemModelDir.exists() && !globals.paths.itemModelDir.mkdirs()) {
                error("Could not create model directory: ${globals.paths.itemModelDir.absolutePath}")
            }


            // Ask for item ID
            val input = Messages.showInputDialog(
                project,
                "Enter the BlockName ID (e.g. copper_block):",
                "Generate Mod Block",
                Messages.getQuestionIcon()
            ) ?: return

            val itemId = input.trim()
                .lowercase()
                .replace(" ", "_")

            require(itemId.matches(Regex("[a-z0-9_]+"))) {
                "Block ID must contain only lowercase letters, numbers, and underscores"
            }

            require(itemId.isNotEmpty()) {
                "Block ID cannot be empty"
            }
            // normalisation
            val displayName = itemId
                .split("_")
                .joinToString(" ") {
                    it.replaceFirstChar { c -> c.uppercase() }
                }

            // Prepare language entry
            val langEntry =
                "\"block.createindustrial.$itemId\": \"$displayName\""

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


            check(marker in globals.contents.javModBlockContent) {
                "NO POINTER FOUND! in ${globals.paths.javModBlockFile.absolutePath}"
            }

            check(marker in globals.contents.javItemContent) {
                "NO POINTER FOUND! in ${globals.paths.javItemFile.absolutePath}"
            }

            check(!globals.contents.javItemContent.contains("ITEMS.registerSimpleItem(\"$itemId\"")) {
                "This item appears to already be registered in ModItems.java"
            }


            val javItemEntry = """
                public static final DeferredItem<BlockItem> ${itemId.uppercase()} =
                        ITEMS.registerSimpleBlockItem("$itemId", ModBlocks.${itemId.uppercase()} );
            """.trimIndent()

            val javModBlockEntry = """
                public static final DeferredBlock<Block> ${itemId.uppercase()} = BLOCKS.registerSimpleBlock(
                        "${itemId}",
                        BlockBehaviour.Properties.of()
                                .sound(SoundType.METAL)
                                .strength(0.5f,1f)
                                .requiresCorrectToolForDrops()
            
                );
            """.trimIndent()


            val updatedJavModBlockContent = globals.contents.javModBlockContent.replaceFirst(
                marker,
                "$$javModBlockEntry\n\n $marker"
            )
            val updatedJavItemContent = globals.contents.javItemContent.replaceFirst(
                marker,
                "$javItemEntry\n\n    $marker"
            )

            // Prepare JSONs
            val modelFile = File(globals.paths.itemModelDir, "$itemId.json")
            val blockStateFile = File(globals.paths.blockStateDir, "$itemId.json")
            val blockModelFile = File(globals.paths.blockModelDir, "$itemId.json")


            check(!modelFile.exists()) {
                "Model file already exists: ${modelFile.name}"
            }

            val modelContent = """
                {
                   "parent": "createindustrial:block/${itemId}"
                }
            """.trimIndent()

            val blockModelContent = """
                {
                 "parent": "minecraft:block/cube_all", "textures": { "all": "createindustrial:block/${itemId}" }
                }
            """.trimIndent()

            // BlockStates
            val blockStateContent = """
                {
                "variants": {"": {"model": "createindustrial:block/${itemId}"}}
                }
            """.trimIndent()

            // Write changes
            globals.paths.langFile.writeText(updatedLang)
            modelFile.writeText(modelContent)
            blockModelFile.writeText(blockModelContent)
            globals.paths.javItemFile.writeText(updatedJavItemContent)
            blockStateFile.writeText(blockStateContent)
            globals.paths.javModBlockFile.writeText(updatedJavModBlockContent)

            Messages.showInfoMessage(
                project,
                "Created item: $itemId\n" +
                        "Display name: $displayName\n" +
                        "Java file: ${globals.paths.javItemFile.absolutePath}\n" +
                        "Model file: ${modelFile.absolutePath}\n" +
                        "BlockState file ${blockStateFile.absolutePath}\n\n"+
                        "Remember to add the matching PNG texture!",
                "Item Generator"
            )

            reloadFile(globals.paths.langFile)
            reloadFile(globals.paths.javItemFile)

        } catch (e: Exception) {
            Messages.showErrorDialog(
                project,
                "${e.javaClass.simpleName}: ${e.message}",
                "BlockGenerator Generator Error"
            )
            e.printStackTrace()
        }
    }
}
