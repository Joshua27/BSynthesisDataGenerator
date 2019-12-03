import datagenerator.OperationDataGenerator
import datagenerator.PredicateDataGenerator
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

private val logger = LoggerFactory.getLogger(PredicateDataGenerator::class.java)

fun main(args: Array<String>) {
    System.setProperty("logback.configurationFile", "config/logging.xml")
    if (args.size != 2) {
        println("One argument expected: Path to the root of the predicate database.")
        return
    }
    val type = args.first()
    val rootPath = Paths.get(args[1])
    if (type == "predicate") {
        predicateDataGeneration(rootPath)
        return
    }
    if (type == "operation") {
        operationDataGeneration(rootPath)
        return
    }
    logger.error("Data generation: Unexpected commandline arguments.")
    return
}

/**
 * Path to the root to be crawled for .mch or .eventb machine operations.
 */
fun operationDataGeneration(rootPath: Path) {
    val dataGenerator = OperationDataGenerator()
    try {
        Files.walk(rootPath).forEach {
            if ((it.toString().endsWith(".mch") || it.toString().endsWith(".eventb")) &&
                !synthesisDataExist(it)
            ) {
                //dataGenerator.generateData(it) TODO
            }
        }
    } catch (e: IOException) {
        logger.error("Could not access source directory {}.", rootPath, e)
    }
}

/**
 * Path to the root of .pdump files with Jannik's predicate data.
 */
fun predicateDataGeneration(rootPath: Path?) {
    val dataGenerator = PredicateDataGenerator()
    try {
        Files.walk(rootPath)
            .parallel().forEach {
                if (it.toString().endsWith("pdump") && !synthesisDataExist(it)) {
                    dataGenerator.generateDataFromDumpFile(it)
                }
            }
    } catch (e: IOException) {
        logger.error("Could not access source directory {}.", rootPath, e)
    }
}

fun synthesisDataExist(dumpSource: Path?) =
    Files.exists(Paths.get(dumpSource?.toString()?.removeSuffix(".pdump") + "_synthesis_data.xml"))
