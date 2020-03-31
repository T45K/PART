package io.github.t45k.part.tracking

import io.github.t45k.part.core.tracking.ParameterTracker
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.SingleVariableDeclaration
import org.eclipse.jdt.core.dom.TypeDeclaration
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ParameterTrackerTest {
    private val testTargetRootPath: Path = Paths.get("./src/test/resources/sample/classes/")

    @Test
    fun testDetectParametersDifferencing() {
        val basicMathMaxParams: List<Pair<String, String>> = parseParams(testTargetRootPath.resolve("BasicMath.java"))
        val basicMathMin: List<Pair<String, String>> = parseParams(testTargetRootPath.resolve("BasicMath.java"), 1)
        val reorderedMathMax: List<Pair<String, String>> = parseParams(testTargetRootPath.resolve("ReorderedMath.java"))
        val sizeChangedMathMax: List<Pair<String, String>> = parseParams(testTargetRootPath.resolve("SizeChangedMath.java"))
        val typeChangedMathMax: List<Pair<String, String>> = parseParams(testTargetRootPath.resolve("TypeChangedMath.java"))

        assert(basicMathMaxParams == basicMathMin)

        val parameterTracker = ParameterTracker()
        assertEquals(ParameterTracker.DiffPattern.REORDERING, parameterTracker.detectParametersDifferencing(basicMathMaxParams, reorderedMathMax))
        assertEquals(ParameterTracker.DiffPattern.SIZE_CHANGED, parameterTracker.detectParametersDifferencing(basicMathMaxParams, sizeChangedMathMax))
        assertEquals(ParameterTracker.DiffPattern.TYPE_CHANGED, parameterTracker.detectParametersDifferencing(basicMathMaxParams, typeChangedMathMax))
    }

    private fun parseParams(path: Path, numberOfMethod: Int = 0): List<Pair<String, String>> {
        val parser: ASTParser = createParser()
        val content = String(Files.readAllBytes(path))
        parser.setSource(content.toCharArray())

        val compilationUnit = parser.createAST(NullProgressMonitor()) as CompilationUnit
        return (compilationUnit.types()[0] as TypeDeclaration)
                .methods[numberOfMethod]
                .parameters()
                .map { param: Any? -> (param as SingleVariableDeclaration).type.toString() to param.name.toString() }
    }

    private fun createParser(): ASTParser {
        val parser: ASTParser = ASTParser.newParser(AST.JLS13)

        @Suppress("UNCHECKED_CAST")
        val options: MutableMap<String, String> = DefaultCodeFormatterConstants.getEclipseDefaultSettings() as MutableMap<String, String>
        options[JavaCore.COMPILER_COMPLIANCE] = JavaCore.VERSION_13
        options[JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM] = JavaCore.VERSION_13
        options[JavaCore.COMPILER_SOURCE] = JavaCore.VERSION_13

        parser.setCompilerOptions(options)
        return parser
    }
}
