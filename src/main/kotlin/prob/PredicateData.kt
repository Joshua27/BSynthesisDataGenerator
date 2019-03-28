package prob

data class VariableState(val variableName: String,
                         val variableType: String,
                         val inputVal: String,
                         val outputVal: String)

data class GroundTruthComponent(val componentName: String,
                                val componentAmount: Int)

data class PredicateData(val examples: Set<Set<VariableState>>,
                         val groundTruth: Set<GroundTruthComponent>)
