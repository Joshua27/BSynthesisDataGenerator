package prob

data class VariableState(
    val variableName: String,
    val variableType: String,
    val value: String
)

data class GroundTruthComponent(
    val componentName: String,
    val componentAmount: Int
)

data class PredicateData(
    val positiveInputs: HashSet<Set<VariableState>>,
    val negativeInputs: HashSet<Set<VariableState>>,
    val groundTruth: HashSet<GroundTruthComponent>
) {
    fun amountOfExamples() = positiveInputs.size + negativeInputs.size
    fun amountOfVariables() =
        if (!positiveInputs.isEmpty()) positiveInputs.first().size else negativeInputs.first().size
}
