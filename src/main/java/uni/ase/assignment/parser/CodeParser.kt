package uni.ase.assignment.parser

import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import uni.ase.assignment.controllers.CanvasController
import uni.ase.assignment.controllers.ConsoleController
import uni.ase.assignment.controllers.LogController
import uni.ase.assignment.parser.structures.Line
import uni.ase.assignment.parser.structures.blocks.Block
import uni.ase.assignment.parser.structures.blocks.BlockType
import uni.ase.assignment.parser.structures.variables.*

/**
 * parses "code" from the main [TextArea] line by line as individual commands
 *
 * @param ca the [TextArea] where the code is written
 * @param cac the [CanvasController] that isnt currently used
 * @param log the [LogController] for writing outputs
 */
class CodeParser (
    val ca: TextArea,
    val cac: CanvasController,
    val log: LogController,
    val cmd: TextField
    ) {
    var coc : ConsoleController = ConsoleController(cmd, cac, log, this)

    var allCode : Block = Block(
        type        = BlockType.MAIN,
        structure   = null,
        range       = 0..ca.text.length,
        code        = ca.text,
        lineRange   = 0..0,
        lines       = mutableListOf(),
        children    = mutableListOf(),
        parent      = null,
        vars        = Variables(
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            log         = log
        ),
        parser      = this,
        log         = log
    )

    //                              name    mutable   value
    var strings   : MutableList<StringVar>  = mutableListOf()
    var integers  : MutableList<IntegerVar> = mutableListOf()
    var doubles   : MutableList<DoubleVar>  = mutableListOf()
    var booleans  : MutableList<BooleanVar> = mutableListOf()
    var arrays    : MutableList<ArrayVar>   = mutableListOf()
    var maps      : MutableList<MapVar>     = mutableListOf()

    val stringRegex : Regex = Regex("\\\"(.*)\\\"|\\'(.*)\\'")
    val doubleRegex : Regex = Regex("\\d+\\.\\d+")
    val integerRegex : Regex = Regex("\\d+")
    val booleanRegex : Regex = Regex("(True|False)")
    val paramOrFunRegex : Regex = Regex("\\\".*\\\"|\\'.*\\'|\\d+\\.\\d+|\\d+|[a-zA-Z0-9_\\\"'\\.\\(\\)\\,\\ ]+")

    val variableDeclarationRegex : Regex = Regex("(?<mutable>var|val)\\s*(?<name>[a-z]\\w*)\\s*:\\s*((?<collectiontype>[A-Z]\\w+<[\\w, \\<\\>]+>)|(?<type>[A-Z]\\w+))\\s*\\=\\s*(?<value>(?<string>\\\".+\\\"|'.+')|(?<double>\\d+\\.\\d+)|(?<integer>\\d+)|(?<function>(?<funcname>\\w+)\\((?<funcparams>.*)\\))|(?<null>null)|(?<boolean>true|false)|(?<collection>\\[.+\\]))")
    val variableUpdateRegex : Regex = Regex("(?<name>\\w+)\\s*(?<method>\\=|\\+\\=|\\-\\=)\\s*(?<value>(?<string>\\\".+\\\"|'.+')|(?<double>\\d+\\.\\d+)|(?<integer>\\d+)|(?<function>(?<funcname>\\w+)\\((?<funcparams>.*)\\))|(?<null>null)|(?<boolean>true|false)|(?<collection>\\[.++\\]))")

    val functionCallRegex : Regex = Regex("(?<name>\\w+)\\((?<params>.*)\\)")

    val functionDeclarationRegex : Regex = Regex("function\\s+(?<name>\\w*)\\((?<params>.*)\\)(\\s*:\\s*(?<returntype>[A-Z]\\w*))?")

    val ifRegex : Regex = Regex("if\\s+\\((?<condition>.*)\\)")
    val elifRegex : Regex = Regex("elif\\s+\\((?<condition>.*)\\)")
    val elseRegex : Regex = Regex("else")

    val forRegex : Regex = Regex("for\\s+(?<params>\\(\\s*(?<param1>.*)\\s*;\\s*(?<param2>.*)\\s*;\\s*(?<param3>.*)\\s*\\)|\\(\\s*(?<parama>.*)\\s*in\\s*(?<paramb>.*)\\s*\\))")
    val whileRegex : Regex = Regex("while\\s+\\((?<condition>.*)\\)")

    val allBetweenBraces : Regex = Regex("(\\{\\s*([^{}]+[^\\s])\\s*\\})")


    fun emptyVarArrays() {
        strings    = mutableListOf()
        integers   = mutableListOf()
        doubles    = mutableListOf()
        booleans   = mutableListOf()
        arrays     = mutableListOf()
        maps       = mutableListOf()
    }

    /**
     * the run method that takes the code from the main [TextArea] and splits it line by line to be processed by each shapes draw class
     */
    fun run() {
        log.out("running code");
        emptyVarArrays()

        var cumulativeChars : Int = 0
        var initialLines = mutableListOf<Line>()

        ca.text.split("\n").forEachIndexed { i, v ->
            val lineLen : Int = v.length
            initialLines.add(Line(
                i,
                IntRange(cumulativeChars, cumulativeChars + lineLen),
                v,
                null,
                null,
                null,
                Block(
                    type        = BlockType.MAIN,
                    structure   = null,
                    range       = 0..ca.text.length,
                    code        = ca.text,
                    lineRange   = 0..initialLines.size,
                    lines       = initialLines,
                    children    = mutableListOf(),
                    parent      = null,
                    vars        = Variables(
                        mutableListOf(),
                        mutableListOf(),
                        mutableListOf(),
                        mutableListOf(),
                        mutableListOf(),
                        mutableListOf(),
                        log         = log
                    ),
                    parser      = this@CodeParser,
                    log         = log
                ),
                log         = log
            ))
            log.out("line: ${initialLines.last().num} range: ${initialLines.last().range} line: ${initialLines.last().line}")
            cumulativeChars+=lineLen+1
        }

        allCode = Block(
            type        = BlockType.MAIN,
            structure   = null,
            range       = 0..ca.text.length,
            code        = ca.text,
            lineRange   = 0..initialLines.size,
            lines       = initialLines,
            children    = mutableListOf(),
            parent      = null,
            vars        = Variables(
                mutableListOf(),
                mutableListOf(),
                mutableListOf(),
                mutableListOf(),
                mutableListOf(),
                mutableListOf(),
                log         = log
            ),
            parser      = this@CodeParser,
            log         = log
        )

//        allCode.lines.forEach { line ->
//            log.out("line: ${line.num} range: ${line.range} line: ${line.line}")
//            parseLine(line.line)
//        }

        var blocks : MutableList<Block> = mutableListOf()

        allBetweenBraces.findAll(allCode.code).forEach { match ->
            var matchRange = match.range
            var matchValue = match.value
            if (match.value.count { it == '{'} != match.value.count { it == '}'}) {
                matchRange = match.range.first until match.range.last - 2
                matchValue = match.value.dropLast(2)
            }
            var subLines = allCode.linesInRange(matchRange)
            blocks.add(
                Block(
                    type        = null,
                    structure   = null,
                    range       = matchRange,
                    code        = matchValue,
                    lineRange   = subLines.first,
                    lines       = subLines.second,
                    children    = mutableListOf(),
                    parent      = null,
                    vars        = Variables(
                        mutableListOf(),
                        mutableListOf(),
                        mutableListOf(),
                        mutableListOf(),
                        mutableListOf(),
                        mutableListOf(),
                        log         = log
                    ),
                    parser      = this@CodeParser,
                    log         = log
                )
            )
        }

        var parentsPossible = true;
        while (parentsPossible) {
            log.out("\n\n\n\n\n\nlooking for parents")
            /** find a block of code that contains any combination of any known block of code */
            val tryFindParentRegex : String = blocks.filter { it.parent == null }.mapIndexed { i, block ->
                    "[^{}]*(?<block${i+1}>${
                        block.code
                            .replace("\n", "\\n")
                            .replace("{", "\\{")
                            .replace("}", "\\}")
                            .replace("(", "\\(")
                            .replace(")", "\\)")
                            .replace("[", "\\[")
                            .replace("]", "\\]")
                    })[^{}]*"
                }.joinToString("|", "(?<block0>\\{((|, )(", "))+\\})")
            log.out("regex to find parents: $tryFindParentRegex")
            var regexResult = Regex(tryFindParentRegex).find(allCode.code)?.groups as? MatchNamedGroupCollection
            log.out(regexResult)

            /** if a block as been found take the parent which will be block0 and find where it is in the main block of code */
            if (regexResult != null) {
                log.out("oh hey its not null")

                if (blocks.map { block -> block.code }.contains(regexResult?.get("block0")?.value)) {
                    val dupedBlock = blocks.filter { it.code == regexResult?.get("block0")?.value }.first()
                    dupedBlock.parent = allCode
                    allCode.children.add(dupedBlock)
                } else {
                    var matchingBlocks = mutableListOf<Block>()

                    val resultRange = Regex(
                        regexResult.get("block0")!!.value
                            .replace("\n", "\\n")
                            .replace("{", "\\{")
                            .replace("}", "\\}")
                            .replace("(", "\\(")
                            .replace(")", "\\)")
                            .replace("[", "\\[")
                            .replace("]", "\\]")
                    ).find(allCode.code)!!.range
                    val resultLines = allCode.linesInRange(resultRange)
                    var parent = Block(
                        type = null,
                        structure   = null,
                        range = regexResult.get("block0")!!.range,
                        code = regexResult.get("block0")!!.value,
                        lineRange = resultLines.first,
                        lines = resultLines.second,
                        children = mutableListOf(),
                        parent = null,
                        vars = Variables(
                            mutableListOf(),
                            mutableListOf(),
                            mutableListOf(),
                            mutableListOf(),
                            mutableListOf(),
                            mutableListOf(),
                            log         = log
                        ),
                        parser = this@CodeParser,
                        log         = log
                    )
                    log.out("the parent is: ${parent.code}")
                    /** now that the parent has been found, find all the blocks that were found within that parent, add them to the parent and add the parent to them */
                    matchingBlocks.addAll(blocks.filter { block ->
                        regexResult.map { result -> result?.value }.intersect(blocks.map { b -> b.code })
                            .contains(block.code)
                    })
                    matchingBlocks.forEach { block -> block.parent = parent }
                    parent.children = matchingBlocks
                    parent.children.forEachIndexed { i, b -> log.out("top level child $i: ${b.code}") }
                    blocks.add(parent)
                }
            } else {
                blocks.filter { block -> block.parent == null }.forEach { orphan ->
                    log.out("\norphan: ${orphan.code}\n\n")
                    val orphanSearchRegex = Regex("(?<allcode>[\\s\\S]*(?<orphanblock>${
                        orphan.code
                            .replace("\n", "\\n")
                            .replace("{", "\\{")
                            .replace("}", "\\}")
                            .replace("(", "\\(")
                            .replace(")", "\\)")
                            .replace("[", "\\[")
                            .replace("]", "\\]")
                    })[\\s\\S]*)")
                    var orphanRes = orphanSearchRegex.find(allCode.code)?.groups as? MatchNamedGroupCollection
                    log.out("$orphanSearchRegex\n\n$orphanRes\n\n")
                    if (orphanRes != null && orphanRes.get("allcode")?.value == allCode.code && orphanRes.get("orphanblock") != null) {
                        orphan.parent = allCode
                        orphan.range = orphanRes.get("orphanblock")!!.range
                        orphan.lineRange = allCode.linesInRange(orphan.range).first
                        orphan.lines = allCode.lines.slice(orphan.lineRange).toMutableList()
                        allCode.children.add(orphan)
                    }
                }
            }
            if (blocks.filter { it.parent == null }.size == 0) { parentsPossible = false }
        }

        allCode.replaceChildrenInCode()
        allCode.defineBlocks()
        allCode.parseLines()

        log.out("\n\n\n\n\n\nprint all child blocks\n\n\n\n\n\n\n\n\n")
        allCode.printChildren()

//        blocks.forEachIndexed { index, block -> log.out("$index: ${block.type} ${block.name} (${block.parameters}) = ${block.code} in range: ${block.range} lines: ${block.lineRange} ${block.lines.joinToString("\n")} contains ${block.children.size} sub blocks has ${block.parent} as a parent and ${block.vars} are the variables in this scope") }

//        log.out("\n\n\n")
//        log.out("\n\n\nall strings")
//        allCode.vars.strings!!.forEachIndexed     { index, stringVar  -> log.out("$index: ${stringVar.name} = ${stringVar.value} is mutable: ${stringVar.mutable}") }
//        log.out("\n\n\nall integers")
//        allCode.vars.integers!!.forEachIndexed    { index, integerVar -> log.out("$index: ${integerVar.name} = ${integerVar.value} is mutable: ${integerVar.mutable}") }
//        log.out("\n\n\nall doubles")
//        allCode.vars.doubles!!.forEachIndexed     { index, doubleVar  -> log.out("$index: ${doubleVar.name} = ${doubleVar.value} is mutable: ${doubleVar.mutable}") }
//        log.out("\n\n\nall booleans")
//        allCode.vars.booleans!!.forEachIndexed    { index, booleanVar -> log.out("$index: ${booleanVar.name} = ${booleanVar.value} is mutable: ${booleanVar.mutable}") }
//        log.out("\n\n\nall arrays")
//        allCode.vars.arrays!!.forEachIndexed      { index, arrayVar   -> log.out("$index: ${arrayVar.type} ${arrayVar.name} = ${arrayVar.array} is mutable: ${arrayVar.mutable}") }
//        log.out("\n\n\nall maps")
//        allCode.vars.maps!!.forEachIndexed        { index, mapVar     -> log.out("$index: <${mapVar.keyType}, ${mapVar.valType}> ${mapVar.name} = ${mapVar.map} is mutable: ${mapVar.mutable}") }
//        log.out("\n\n\nall blocks")
//        allCode.subBlocks!!.forEachIndexed        { index, block     -> log.out("$index: ${block.type} ${block.name} (${block.parameters}) = ${block.code} in range: ${block.range} lines: ${block.lineRange} ${block.lines.joinToString("\n")} contains ${block.subBlocks.size} sub blocks has ${block.parent} as a parent and ${block.vars} are the variables in this scope") }
    }
}