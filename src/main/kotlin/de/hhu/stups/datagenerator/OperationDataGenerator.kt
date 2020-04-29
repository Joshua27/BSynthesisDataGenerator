package de.hhu.stups.datagenerator

import com.google.inject.Guice
import com.google.inject.Stage
import de.hhu.stups.injector.DataGeneratorModule
import de.hhu.stups.prob.SynthesisDataFromOperationCommand
import de.prob.MainModule
import de.prob.exception.ProBError
import de.prob.model.representation.Machine
import de.prob.scripting.Api
import de.prob.statespace.StateSpace
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths

class OperationDataGenerator : DataGenerator() {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val injector = Guice.createInjector(
        Stage.PRODUCTION,
        DataGeneratorModule(), MainModule()
    )

    fun generateData(sourceFile: Path) {
        logger.info("Generating data from B/Event-B machine {}.", sourceFile)
        val generatedData = hashSetOf<OperationData>()
        try {
            val api = injector.getInstance(Api::class.java)
            val stateSpace = loadStateSpace(api, sourceFile)
            val sourceFileNoExt = sourceFile.toString().removeSuffix(".mch").removeSuffix(".eventb")
            val machineName = (stateSpace.mainComponent as Machine).name

            stateSpace.loadedMachine.operationNames.forEach {
                val metaData =
                    MetaData(
                        sourceFile.toString(),
                        machineName,
                        "", // TODO: maybe add hashcode of operation's AST
                        it
                    )
                val operationData = generateDataFromOperation(metaData, stateSpace)
                generatedData.addAll(operationData)
            }
            stateSpace.kill()
            // generated synthesis data is stored in the same folder as the input file
            val target = Paths.get("${sourceFileNoExt}_synthesis_data.xml")
            writeOperationDataSetToFile(generatedData, target)
        } catch (e: Exception) {
            when (e) {
                is DataGeneratorException, is IOException ->
                    logger.error("Could not translate dump file {}.", sourceFile, e)
                else -> logger.error("Unknown exception {}.", sourceFile, e)
            }
        }
    }

    private fun generateDataFromOperation(
        metaData: MetaData,
        stateSpace: StateSpace
    ): Set<OperationData> {
        return try {
            val generateDataCommand =
                SynthesisDataFromOperationCommand(
                    metaData, 10, 10000
                )
            stateSpace.execute(generateDataCommand)
            generateDataCommand.operationDataSet
        } catch (e: ProBError) {
            logger.error("ProB Error when generating data for operation: $e")
            emptySet()
        }
    }
}