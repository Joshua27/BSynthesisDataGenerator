import datagenerator.PredicateDataGenerator
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

private val logger = LoggerFactory.getLogger(PredicateDataGenerator::class.java)

fun main(args: Array<String>) {
    System.setProperty("logback.configurationFile", "config/logging.xml")
    if (args.size != 1) {
        println("One argument expected: Path to the root of the predicate database.")
        return
    }
    val rootPath = Paths.get(args.first())
    val dataGenerator = PredicateDataGenerator()
    try {
        Files.walk(rootPath).parallel().forEach {
            if (it.toString().endsWith("dump") && !synthesisDataExist(it)) {
                dataGenerator.generateDataFromDumpFile(it)
            }
        }
    } catch (e: IOException) {
        logger.error("Could not access source directory {}.", rootPath, e)
    }
}

fun synthesisDataExist(dumpSource: Path?) =
    Files.exists(Paths.get(dumpSource?.toString()?.removeSuffix(".pdump") + "_synthesis_data.xml"))
