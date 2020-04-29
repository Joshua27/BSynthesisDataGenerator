package de.hhu.stups.prob

import de.hhu.stups.datagenerator.IOExample
import de.hhu.stups.datagenerator.MetaData
import de.hhu.stups.datagenerator.OperationData
import de.prob.animator.command.AbstractCommand
import de.prob.parser.BindingGenerator
import de.prob.parser.ISimplifiedROMap
import de.prob.prolog.output.IPrologTermOutput
import de.prob.prolog.term.PrologTerm

/**
 * Generate data from all operations for a given machine path.
 * The data set consists of a set of positive and a set of negative states describing the
 * The ground truth for a single operation consists of the operators used in the operation separated by variables
 * with the specific amount of usages, e.g., [(v1, [(member,2)])].
 * Special identifiers are 'global_ground_truth_vars' and 'global_ground_truth_params' with the bundled ground truth
 * of an operation's body and the ground truth of its parameters respectively.
 */
class SynthesisDataFromOperationCommand(private val metaData: MetaData) : AbstractCommand(), SynthesisDataCommand {

    companion object {
        private const val PROLOG_COMMAND_NAME = "generate_data_from_machine_operation_"
        private const val OPERATION_DATA = "OperationData"
    }

    private var augmentations = 10
    private var solverTimeoutMs = 10000

    val operationDataSet = hashSetOf<OperationData>()

    constructor(metaData: MetaData, augmentations: Int)
            : this(metaData) {
        this.augmentations = augmentations
    }

    constructor(metaData: MetaData, augmentations: Int, solverTimeoutMs: Int)
            : this(metaData, augmentations) {
        this.solverTimeoutMs = solverTimeoutMs
    }

    override fun processResult(bindings: ISimplifiedROMap<String, PrologTerm>?) {
        // result is a Prolog list of triples (DataVariations, OperationName, VarGroundTruths)
        val operationDataArg = bindings?.get(OPERATION_DATA)
        BindingGenerator.getList(operationDataArg).forEach {
            val dataVariations =
                BindingGenerator.getList(BindingGenerator.getCompoundTerm(it, ",", 3).getArgument(1))
            val varGts =
                BindingGenerator.getList(BindingGenerator.getCompoundTerm(it, ",", 3).getArgument(3))
            val processedGt = processGroundTruth(varGts)
            dataVariations.forEach { record ->
                val operationData =
                    OperationData(metaData, hashSetOf(), hashMapOf())
                val prologTuples = BindingGenerator.getList(record)
                prologTuples.forEach { ioTuple ->
                    val input = processState(BindingGenerator.getList(ioTuple.getArgument(0)))
                    val output = processState(BindingGenerator.getList(ioTuple.getArgument(1)))
                    operationData.examples.add(IOExample(input, output))
                }
                operationData.varGroundTruths.putAll(processedGt)
                operationDataSet.add(operationData)
            }
        }
    }

    override fun writeCommand(pto: IPrologTermOutput?) {
        print(".")
        pto?.openTerm(PROLOG_COMMAND_NAME)
            ?.printAtom(metaData.operationName)
            ?.printNumber(augmentations.toLong())
            ?.printNumber(solverTimeoutMs.toLong())
            ?.printAtom(metaData.machinePath)
            ?.printAtom(metaData.machineName)
            ?.printVariable(OPERATION_DATA)
            ?.closeTerm()
        println("prob2_interface:" + pto.toString())
    }
}