package datagenerator

import com.google.inject.Guice
import com.google.inject.Stage
import de.prob.MainModule
import de.prob.scripting.Api
import de.prob.scripting.ModelTranslationError
import de.prob.statespace.StateSpace
import injector.DataGeneratorModule
import org.slf4j.LoggerFactory
import prob.PredicateData
import prob.SynthesisDataFromPredicateCommand
import java.io.IOException
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class DataGenerator {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val dataGeneratorModule = DataGeneratorModule()
    private val injector = Guice.createInjector(Stage.PRODUCTION, dataGeneratorModule, MainModule())
    private val api = injector.getInstance(Api::class.java)

    @Throws(DataGeneratorException::class)
    fun generateDataFromDumpEntry(source: Path, dataDumpEntry: String) =
        RawDataSet(dataDumpEntry.split(":")[1], source)

    @Throws(DataGeneratorException::class)
    private fun loadStateSpace(file: Path): StateSpace {
        logger.info("\tLoading state space for {}", file)
        try {
            val fileName = file.toString()
            return when {
                fileName.endsWith(".bcm") -> api.eventb_load(file.toString())
                fileName.endsWith(".mch") -> api.b_load(file.toString())
                else -> throw DataGeneratorException("Unknown machine type.")
            }
        } catch (e: ModelTranslationError) {
            throw DataGeneratorException("Unable to load state space due to model translation error.")
        }
    }

    @Throws(IOException::class)
    private fun collectDataFromDumpFile(sourceFile: Path): Set<RawDataSet> {
        val machineFile: Path
        val data = hashSetOf<RawDataSet>()
        val lines = Files.lines(sourceFile).iterator()
        if (!lines.hasNext()) {
            return emptySet()
        }
        val next = lines.next()
        if (next.startsWith("#source")) {
            try {
                machineFile = Paths.get(next.substring(8))
                lines.forEachRemaining { l ->
                    data.add(generateDataFromDumpEntry(machineFile, l))
                }
            } catch (e: DataGeneratorException) {
                logger.warn("Could not translate data from dump entry in file {}.", sourceFile)
            }
        }
        return data
    }

    fun generateDataFromDumpFile(sourceFile: Path) {
        logger.info("Translating dump file {}.", sourceFile)
        val rawData: Set<RawDataSet>
        val generatedData = hashSetOf<PredicateData>()
        try {
            rawData = collectDataFromDumpFile(sourceFile)
            if (rawData.isEmpty()) {
                logger.info("No training data found.")
                return
            }
            val stateSpace = loadStateSpace(rawData.first().source) // TODO: parallelise
            rawData.forEach { generatedData.add(generateDataFromPredicate(stateSpace, it)) }
            // generated synthesis data is stored in the same folder as the input file
            val target = Paths.get("${pathWithoutMachineExtension(sourceFile)}_synthesis_data.xml")
            writePredicateDataSetToFile(generatedData, target)
        } catch (e: Exception) {
            when (e) {
                is DataGeneratorException, is IOException ->
                    logger.error("Could not translate dump file {}.", sourceFile, e)
                else -> logger.error("Unknown exception {}.", sourceFile, e)
            }
        }
    }

    private fun pathWithoutMachineExtension(sourceFile: Path) =
        sourceFile.toString().removeSuffix(".bcm").removeSuffix(".mch")

    private fun generateDataFromPredicate(stateSpace: StateSpace, rawDataSet: RawDataSet): PredicateData {
        val generateDataCommand = SynthesisDataFromPredicateCommand(rawDataSet.predicateAst)
        stateSpace.execute(generateDataCommand)
        generateDataCommand.predicateData
        // TODO
        return PredicateData(hashSetOf(), hashSetOf())
    }
}