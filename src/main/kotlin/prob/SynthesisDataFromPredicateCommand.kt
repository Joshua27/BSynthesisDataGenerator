package prob

import de.prob.animator.command.AbstractCommand
import de.prob.parser.BindingGenerator
import de.prob.parser.ISimplifiedROMap
import de.prob.prolog.output.IPrologTermOutput
import de.prob.prolog.term.ListPrologTerm
import de.prob.prolog.term.PrologTerm

/**
 * Generate data from a raw B or Event-B predicate like 'x: Int & y: Int & x > y' to predict components for
 * inductive program synthesis. The data set consists of a set of positive and a set of negative states describing the
 * behavior of the predicate. The ground truth consists of the operators used in the predicate with the specific amount
 * of usages, e.g., [(member,2), (integer_set,2), (conjunct, 2), (greater, 1)] for above predicate.
 */
class SynthesisDataFromPredicateCommand(private val rawPredicate: String) : AbstractCommand() {

    companion object {
        private const val PROLOG_COMMAND_NAME = "generate_synthesis_data_from_predicate"
        private const val PREDICATE_DATA = "PredicateData"
    }

    val predicateData = PredicateData(hashSetOf(), hashSetOf(), hashSetOf())

    override fun processResult(bindings: ISimplifiedROMap<String, PrologTerm>?) {
        if (bindings?.get(PREDICATE_DATA)?.functor == "no_data") {
            return
        }
        // result is a Prolog list of triples (PositiveInputs, NegativeInputs, GroundTruth)
        val predicateDataArg = bindings?.get(PREDICATE_DATA)
        BindingGenerator.getList(predicateDataArg).forEach {
            // positive and negative input examples from Prolog are a list of lists
            val positiveInputs =
                BindingGenerator.getList(BindingGenerator.getCompoundTerm(it, 1))
            val negativeInputs =
                BindingGenerator.getList(BindingGenerator.getCompoundTerm(it, 2))
            positiveInputs.forEach { input ->
                predicateData.positiveInputs.add(processInputState(BindingGenerator.getList(input)))
            }
            negativeInputs.forEach { input ->
                predicateData.negativeInputs.add(processInputState(BindingGenerator.getList(input)))
            }
            processGroundTruth(BindingGenerator.getList(BindingGenerator.getCompoundTerm(it, 3)))
        }
    }

    private fun processInputState(input: ListPrologTerm?): Set<VariableState> {
        val inputSet = hashSetOf<VariableState>()
        input?.forEach { varState ->
            val varName = BindingGenerator.getCompoundTerm(varState, 1).toString()
            val varType = BindingGenerator.getCompoundTerm(varState, 2).toString()
            val varValue = BindingGenerator.getCompoundTerm(varState, 3).toString()
            inputSet.add(VariableState(varName, varType, varValue))
        }
        return inputSet
    }

    // ground truth is a Prolog list of tuples (ComponentName, ComponentAmount)
    private fun processGroundTruth(groundTruth: ListPrologTerm?) =
        groundTruth?.forEach {
            predicateData.groundTruth.add(
                GroundTruthComponent(
                    BindingGenerator.getCompoundTerm(it, 1).toString(),
                    Integer.parseInt(BindingGenerator.getCompoundTerm(it, 2).toString())
                )
            )
        }

    override fun writeCommand(pto: IPrologTermOutput?) {
        pto?.openTerm(PROLOG_COMMAND_NAME)
            ?.printAtom(rawPredicate)
            ?.printVariable(PREDICATE_DATA)
            ?.closeTerm()
    }
}