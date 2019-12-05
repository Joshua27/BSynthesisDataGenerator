package de.hhu.stups.datagenerator

import de.prob.prolog.term.PrologTerm
import java.nio.file.Path

data class Variable(
    val name: String,
    val type: String
)

data class VariableState(
    val variable: Variable,
    val value: String
)

data class IOExample(
    val input: HashSet<VariableState>,
    val output: HashSet<VariableState>
)

data class GroundTruthComponent(
    val componentName: String,
    val componentAmount: Int
)

data class MetaData(
    val machinePath: String,
    val machineName: String,
    val predicateHash: String,
    var operationName: String
)

data class RawDataSet(val predicateAst: PrologTerm, val source: Path)

data class PredicateData(
    val metaData: MetaData,
    val positiveInputs: HashSet<Set<VariableState>>,
    val negativeInputs: HashSet<Set<VariableState>>,
    val groundTruth: HashSet<GroundTruthComponent>
) {
    fun amountOfExamples() = positiveInputs.size + negativeInputs.size

    fun amountOfVariables() = if (positiveInputs.isNotEmpty()) positiveInputs.first().size
    else negativeInputs.first().size

    fun getVariables() = if (positiveInputs.isNotEmpty()) positiveInputs.first().map { it.variable }
    else negativeInputs.first().map { it.variable }
}

data class OperationData(
    val metaData: MetaData,
    val examples: HashSet<IOExample>,
    val varGroundTruths: HashMap<String, HashSet<GroundTruthComponent>>
) {
    fun amountOfExamples() = examples.size

    fun amountOfVariables() = if (examples.isNotEmpty()) examples.first().input.size
    else 0

    fun getVariables() = if (examples.isNotEmpty()) examples.first().input.map { it.variable }
    else emptySet<Variable>()
}
