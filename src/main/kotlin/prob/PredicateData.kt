package prob

data class VariableState(val variableName: String,
                         val variableType: String,
                         val value: String)

data class GroundTruthComponent(val componentName: String,
                                val componentAmount: Int)

data class PredicateData(val positiveInputs: HashSet<Set<VariableState>>,
                         val negativeInputs: HashSet<Set<VariableState>>,
                         val groundTruth: HashSet<GroundTruthComponent>)
