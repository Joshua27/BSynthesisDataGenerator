import datagenerator.DataGenerator
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

private val logger = LoggerFactory.getLogger(DataGenerator::class.java)

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("One argument expected: Path to the root of the predicate database.")
        return
    }
    val rootPath = Paths.get(args.first())
    try {
        Files.walk(rootPath).use { stream ->
            stream.filter { p -> Files.isRegularFile(p) }
                .filter { p -> p.toString().endsWith("dump") }
                .forEach { p -> DataGenerator().generateDataFromDumpFile(p) }

        }
    } catch (e: IOException) {
        logger.error("Could not access source directory {}.", rootPath, e)
    }
}