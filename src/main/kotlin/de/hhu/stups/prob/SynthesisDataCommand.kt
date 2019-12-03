package de.hhu.stups.prob

import de.hhu.stups.datagenerator.GroundTruthComponent
import de.hhu.stups.datagenerator.Variable
import de.hhu.stups.datagenerator.VariableState
import de.prob.parser.BindingGenerator
import de.prob.prolog.term.ListPrologTerm

interface SynthesisDataCommand {

    fun processState(state: ListPrologTerm?): HashSet<VariableState> {
        val stateSet = hashSetOf<VariableState>()
        state?.forEach { varState ->
            val varName = BindingGenerator.getCompoundTerm(varState, 2).getArgument(1).toString()
            val nestedTuple = BindingGenerator.getCompoundTerm(varState, 2).getArgument(2)
            val varType = BindingGenerator.getCompoundTerm(nestedTuple, 2).getArgument(1).toString()
            val varValue = BindingGenerator.getCompoundTerm(nestedTuple, 2).getArgument(2).toString()
                .removePrefix("'").removeSuffix("'")
            stateSet.add(
                VariableState(
                    Variable(
                        varName,
                        varType
                    ), varValue
                )
            )
        }
        return stateSet
    }

    // ground truth is a Prolog list of tuples (ComponentName, ComponentAmount)
    fun processGroundTruth(groundTruth: HashSet<GroundTruthComponent>, prologGroundTruth: ListPrologTerm?) {
        prologGroundTruth?.forEach {
            groundTruth.add(
                GroundTruthComponent(
                    BindingGenerator.getCompoundTerm(it, 2).getArgument(1).toString(),
                    Integer.parseInt(BindingGenerator.getCompoundTerm(it, 2).getArgument(2).toString())
                )
            )
        }
    }

    // ground truth is a Prolog list of tuples of variable name and ground truth as above
    fun processGroundTruth(prologGroundTruth: ListPrologTerm?):
            HashMap<String, HashSet<GroundTruthComponent>> {
        val varGts = hashMapOf<String, HashSet<GroundTruthComponent>>()
        prologGroundTruth?.forEach {
            val varName = BindingGenerator.getCompoundTerm(it, ",", 1).functor
            val groundTruth = hashSetOf<GroundTruthComponent>()
            processGroundTruth(
                groundTruth,
                BindingGenerator.getList(BindingGenerator.getCompoundTerm(it, ",", 2))
            )
            varGts[varName] = groundTruth
        }
        return varGts
    }
}

