package de.hhu.stups.prob

import de.hhu.stups.datagenerator.MetaData
import de.hhu.stups.datagenerator.PredicateData
import de.prob.animator.command.AbstractCommand
import de.prob.parser.BindingGenerator
import de.prob.parser.ISimplifiedROMap
import de.prob.prolog.output.IPrologTermOutput
import de.prob.prolog.term.PrologTerm

/**
 * Generate data from an untyped B or Event-B predicate AST to predict components for inductive program synthesis.
 * The data set consists of a set of positive and a set of negative states describing the
 * behavior of the predicate. The ground truth consists of the operators used in the predicate with the specific amount
 * of usages, e.g., [(member,2), (integer_set,2), (conjunct, 2), (greater, 1)] for above predicate.
 */
class SynthesisDataFromPredicateCommand(private val metaData: MetaData, private val untypedPredicate: PrologTerm) :
    AbstractCommand(), SynthesisDataCommand {

    companion object {
        private const val PROLOG_COMMAND_NAME = "generate_synthesis_data_from_predicate_untyped_"
        private const val PREDICATE_DATA = "PredicateData"
    }

    private var augmentations = 10
    private var solverTimeoutMs = 10000

    val predicateDataSet = hashSetOf<PredicateData>()

    constructor(metaData: MetaData, untypedPredicate: PrologTerm, augmentations: Int)
            : this(metaData, untypedPredicate) {
        this.augmentations = augmentations
    }

    constructor(metaData: MetaData, untypedPredicate: PrologTerm, augmentations: Int, solverTimeoutMs: Int)
            : this(metaData, untypedPredicate, augmentations) {
        this.solverTimeoutMs = solverTimeoutMs
    }

    override fun processResult(bindings: ISimplifiedROMap<String, PrologTerm>?) {
        // result is a Prolog list of triples (PositiveInputs, NegativeInputs, GroundTruth)
        val predicateDataArg = bindings?.get(PREDICATE_DATA)
        BindingGenerator.getList(predicateDataArg).forEach {
            val predicateData = PredicateData(
                metaData,
                hashSetOf(),
                hashSetOf(),
                hashSetOf()
            )
            // positive and negative input examples from Prolog are a list of lists
            val positiveInputs =
                BindingGenerator.getList(BindingGenerator.getCompoundTerm(it, 2).getArgument(1))
            // triple from prolog is two nested tuples
            val nestedTuple =
                BindingGenerator.getCompoundTerm(BindingGenerator.getCompoundTerm(it, 2).getArgument(2), ",", 2)
            val negativeInputs =
                BindingGenerator.getList(nestedTuple.getArgument(1))
            positiveInputs.forEach { input ->
                predicateData.positiveInputs.add(processState(BindingGenerator.getList(input)))
            }
            negativeInputs.forEach { input ->
                predicateData.negativeInputs.add(processState(BindingGenerator.getList(input)))
            }
            processGroundTruth(
                predicateData.groundTruth,
                BindingGenerator.getList(nestedTuple.getArgument(2))
            )
            predicateDataSet.add(predicateData)
        }
    }

    override fun writeCommand(pto: IPrologTermOutput?) {
        print(".")
        pto?.openTerm(PROLOG_COMMAND_NAME)
            ?.printAtom(metaData.machinePath)
            ?.printNumber(augmentations.toLong())
            ?.printNumber(solverTimeoutMs.toLong())
            ?.printTerm(untypedPredicate)
            ?.printVariable(PREDICATE_DATA)
            ?.closeTerm()
        println("prob2_interface:" + pto.toString())
    }
}