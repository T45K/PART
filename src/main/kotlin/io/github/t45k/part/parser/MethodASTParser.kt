package io.github.t45k.part.parser

import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM
import org.eclipse.jdt.core.JavaCore.COMPILER_COMPLIANCE
import org.eclipse.jdt.core.JavaCore.COMPILER_SOURCE
import org.eclipse.jdt.core.JavaCore.VERSION_13
import org.eclipse.jdt.core.dom.AST.JLS13
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.TypeDeclaration
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.getEclipseDefaultSettings
import java.nio.file.Files
import java.nio.file.Path

class MethodASTParser(mjavaFilePath: Path) {
    private val contents: String = String(Files.readAllBytes(mjavaFilePath))

    fun parse(): MethodDeclaration {
        val parser: ASTParser = createParser()
        parser.setSource(contents.toCharArray())

        val typeDeclaration: TypeDeclaration = parser.createAST(NullProgressMonitor()) as TypeDeclaration
        return typeDeclaration.methods[0]
    }

    private fun createParser(): ASTParser {
        val parser: ASTParser = ASTParser.newParser(JLS13)
        parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS)

        @Suppress("UNCHECKED_CAST")
        val options: MutableMap<String, String> = getEclipseDefaultSettings() as MutableMap<String, String>
        options[COMPILER_COMPLIANCE] = VERSION_13
        options[COMPILER_CODEGEN_TARGET_PLATFORM] = VERSION_13
        options[COMPILER_SOURCE] = VERSION_13

        parser.setCompilerOptions(options)
        return parser
    }
}
