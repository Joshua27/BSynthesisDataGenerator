package prob

import de.prob.animator.command.AbstractCommand
import de.prob.parser.BindingGenerator
import de.prob.parser.ISimplifiedROMap
import de.prob.prolog.output.IPrologTermOutput
import de.prob.prolog.term.PrologTerm

class SynthesisDataFromPredicateCommand(private val rawPredicate: String) : AbstractCommand() {

    companion object {
        private const val PROLOG_COMMAND_NAME = "generate_synthesis_data_from_predicate"
        private const val PREDICATE_DATA = "PredicateData"
    }

    lateinit var predicateData: PredicateData

    override fun processResult(bindings: ISimplifiedROMap<String, PrologTerm>?) {
        if (bindings?.get(PREDICATE_DATA)?.functor == "no_data") {
            return
        }
        val predicateDataArg = bindings?.get(PREDICATE_DATA)
        // result is a Prolog tuple (Examples, GroundTruth)
        val processedExamples: Set<Set<VariableState>> = hashSetOf()
        // examples from Prolog are a list of lists
        val examplesTerm =
            BindingGenerator.getList(BindingGenerator.getCompoundTerm(predicateDataArg, 1))
        examplesTerm.forEach { println(it) } // TODO
        // ground truth is a Prolog list of tuples (ComponentName, ComponentAmount)
        val groundTruthTerm =
            BindingGenerator.getList(BindingGenerator.getCompoundTerm(predicateDataArg, 2))
        groundTruthTerm.forEach { println(it) } // TODO
    }

    override fun writeCommand(pto: IPrologTermOutput?) {
        pto?.openTerm(PROLOG_COMMAND_NAME)
            ?.printAtom(rawPredicate)
            ?.printVariable(PREDICATE_DATA)
            ?.closeTerm()
    }
}