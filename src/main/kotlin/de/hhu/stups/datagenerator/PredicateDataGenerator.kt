package de.hhu.stups.datagenerator

import com.google.inject.Guice
import com.google.inject.Stage
import de.be4.classicalb.core.parser.ClassicalBParser
import de.hhu.stups.injector.DataGeneratorModule
import de.hhu.stups.prob.SynthesisDataFromPredicateCommand
import de.prob.MainModule
import de.prob.exception.ProBError
import de.prob.model.representation.Machine
import de.prob.parserbase.ProBParserBaseAdapter
import de.prob.scripting.Api
import de.prob.scripting.ModelTranslationError
import de.prob.statespace.StateSpace
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class PredicateDataGenerator {
    companion object {
        private const val PROB_EXAMPLES_DIR = "/home/joshua/STUPS/"
    }

    private val logger = LoggerFactory.getLogger(javaClass)
    private val parserBaseAdapter = ProBParserBaseAdapter(ClassicalBParser())
    private val injector = Guice.createInjector(
        Stage.PRODUCTION,
        DataGeneratorModule(), MainModule()
    )

    private fun pathToProbExamples(source: Path) =
        source.toString().removePrefix("examples/")

    @Throws(DataGeneratorException::class)
    private fun getRawDataSetFromDumpEntry(source: Path, dataDumpEntry: String) =
        RawDataSet(
            parserBaseAdapter.parsePredicate(dataDumpEntry.substring(dataDumpEntry.indexOf(':') + 1), false),
            Paths.get(PROB_EXAMPLES_DIR + pathToProbExamples(source))
        )

    @Throws(DataGeneratorException::class)
    private fun loadStateSpace(api: Api, file: Path): StateSpace {
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
                while (lines.hasNext()) {
                    data.add(getRawDataSetFromDumpEntry(machineFile, lines.next()))
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
            val api = injector.getInstance(Api::class.java)
            val stateSpace = loadStateSpace(api, rawData.first().source)
            val sourceFileNoExt = sourceFile.toString().removeSuffix(".pdump")
            rawData.forEach {
                val metaData =
                    MetaData(
                        it.source.toString(),
                        (stateSpace.mainComponent as Machine).name,
                        getSHA(it.predicateAst.toString()), ""
                    )
                val predicateData = generateDataFromPredicate(metaData, stateSpace, it)
                generatedData.addAll(predicateData)
            }
            stateSpace.kill()
            // generated synthesis data is stored in the same folder as the input file
            val target = Paths.get("${sourceFileNoExt}_synthesis_data.xml")
            writePredicateDataSetToFile(generatedData, target)
        } catch (e: Exception) {
            when (e) {
                is DataGeneratorException, is IOException ->
                    logger.error("Could not translate dump file {}.", sourceFile, e)
                else -> logger.error("Unknown exception {}.", sourceFile, e)
            }
        }
    }

    private fun generateDataFromPredicate(
        metaData: MetaData,
        stateSpace: StateSpace,
        rawDataSet: RawDataSet
    ): Set<PredicateData> {
        return try {
            val generateDataCommand =
                SynthesisDataFromPredicateCommand(
                    metaData, rawDataSet.predicateAst, 5, 10000
                )
            stateSpace.execute(generateDataCommand)
            generateDataCommand.predicateDataSet
        } catch (e: ProBError) {
            logger.error("ProB Error when generating data for Pred: $e")
            emptySet()
        }
    }
}