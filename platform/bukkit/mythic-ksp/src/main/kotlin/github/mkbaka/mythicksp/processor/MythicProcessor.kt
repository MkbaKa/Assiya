package github.mkbaka.mythicksp.processor

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate

class MythicProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    private val mechanics = linkedMapOf<String, String>()
    private val conditions = linkedMapOf<String, String>()
    private val variants = linkedMapOf<String, ConditionVariant>()
    private val sourceFiles = linkedSetOf<KSFile>()
    private var invalid = false
    private var skipGeneration = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (options["mythicksp.generatedPackage"] == null) {
            logger.error("Missing mythicksp.generatedPackage")
            invalid = true
            return emptyList()
        }
        val hookClass = options["mythicksp.hookClass"]
        if (hookClass == null) {
            logger.error("Missing mythicksp.hookClass")
            invalid = true
            return emptyList()
        }
        val hook = resolver.getClassDeclarationByName(resolver.getKSNameFromString(hookClass))
        // Test compilations reuse the main compilation's generated function.
        skipGeneration = hook?.containingFile == null
        if (skipGeneration) return emptyList()
        val deferred = mutableListOf<KSAnnotated>()
        collect(resolver, "MythicMechanic", mechanics, deferred)
        collect(resolver, "MythicCondition", conditions, deferred)
        collectVariants(resolver, deferred)
        return deferred
    }

    private fun collectVariants(resolver: Resolver, deferred: MutableList<KSAnnotated>) {
        val annotationType = "github.mkbaka.mythicksp.annotation.MythicConditionVariant"
        for (symbol in resolver.getSymbolsWithAnnotation(annotationType)) {
            if (!symbol.validate()) {
                deferred.add(symbol)
                continue
            }
            val declaration = symbol as? KSClassDeclaration
            if (declaration == null || declaration.classKind != ClassKind.CLASS ||
                Modifier.ABSTRACT in declaration.modifiers || Modifier.INNER in declaration.modifiers ||
                declaration.typeParameters.isNotEmpty() || !isAccessible(declaration)
            ) {
                fail("@MythicConditionVariant requires an accessible, concrete, non-inner class without type parameters", symbol)
                continue
            }
            val annotation = declaration.annotations.first {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationType
            }
            val base = (annotation.arguments.first { it.name?.asString() == "base" }.value as? KSType)
                ?.declaration as? KSClassDeclaration
            val baseName = base?.qualifiedName?.asString()
            val required = annotation.arguments.first { it.name?.asString() == "requiresClass" }.value as String
            if (baseName == null || base.annotations.none {
                    it.annotationType.resolve().declaration.qualifiedName?.asString() ==
                        "github.mkbaka.mythicksp.annotation.MythicCondition"
                } || declaration.getAllSuperTypes().none { it.declaration.qualifiedName?.asString() == baseName }
            ) {
                fail("@MythicConditionVariant must extend a @MythicCondition base class", symbol)
                continue
            }
            if (required.isBlank()) {
                fail("@MythicConditionVariant requiresClass must not be blank", symbol)
                continue
            }
            if (declaration.annotations.any {
                    it.annotationType.resolve().declaration.qualifiedName?.asString() ==
                        "github.mkbaka.mythicksp.annotation.MythicCondition"
                }) {
                fail("A condition variant must not declare @MythicCondition names", symbol)
                continue
            }
            val variant = ConditionVariant(binaryName(declaration), required)
            val previous = variants.putIfAbsent(baseName, variant)
            if (previous != null && previous != variant) {
                fail("Only one condition variant is allowed for $baseName", symbol)
            }
            declaration.containingFile?.let(sourceFiles::add)
            base?.containingFile?.let(sourceFiles::add)
        }
    }

    private fun binaryName(declaration: KSClassDeclaration): String {
        val parent = declaration.parentDeclaration as? KSClassDeclaration
        return if (parent == null) {
            declaration.qualifiedName!!.asString()
        } else {
            binaryName(parent) + "$" + declaration.simpleName.asString()
        }
    }

    private fun collect(
        resolver: Resolver,
        annotationName: String,
        entries: MutableMap<String, String>,
        deferred: MutableList<KSAnnotated>
    ) {
        val annotationType = "github.mkbaka.mythicksp.annotation.$annotationName"
        for (symbol in resolver.getSymbolsWithAnnotation(annotationType)) {
            if (!symbol.validate()) {
                deferred.add(symbol)
                continue
            }
            val declaration = symbol as? KSClassDeclaration
            if (declaration == null || declaration.classKind != ClassKind.CLASS ||
                Modifier.ABSTRACT in declaration.modifiers || Modifier.INNER in declaration.modifiers ||
                declaration.typeParameters.isNotEmpty() || !isAccessible(declaration)
            ) {
                fail("@$annotationName requires an accessible, concrete, non-inner class without type parameters", symbol)
                continue
            }
            val className = declaration.qualifiedName?.asString() ?: continue
            val annotation = declaration.annotations.first {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationType
            }
            val names = annotation.arguments.firstOrNull { it.name?.asString() == "names" }?.value as? List<*>
            if (names.isNullOrEmpty()) {
                fail("@$annotationName requires at least one name", symbol)
                continue
            }
            declaration.containingFile?.let(sourceFiles::add)
            for (value in names) {
                val name = (value as? String)?.lowercase()
                if (name.isNullOrBlank()) {
                    fail("@$annotationName names must not be blank", symbol)
                    continue
                }
                val previous = entries.putIfAbsent(name, className)
                if (previous != null && previous != className) {
                    fail("Duplicate @$annotationName name '$name': $previous and $className", symbol)
                }
            }
        }
    }

    private fun isAccessible(declaration: KSDeclaration): Boolean {
        var current: KSDeclaration? = declaration
        while (current != null) {
            if (Modifier.PRIVATE in current.modifiers || Modifier.PROTECTED in current.modifiers) return false
            current = current.parentDeclaration
        }
        return true
    }

    private fun fail(message: String, symbol: KSAnnotated) {
        invalid = true
        logger.error(message, symbol)
    }

    override fun finish() {
        if (invalid || skipGeneration) return
        for (base in variants.keys) {
            if (base !in conditions.values) {
                logger.error("Condition variant base $base must be registered in the same compilation")
                return
            }
        }
        val generatedPackage = options["mythicksp.generatedPackage"] ?: return
        codeGenerator.createNewFile(
            Dependencies(true, *sourceFiles.toTypedArray()), generatedPackage, "MythicRegistration"
        ).bufferedWriter().use {
            it.write(renderRegistrations(generatedPackage, options.getValue("mythicksp.hookClass"), mechanics, conditions, variants))
        }
    }
}
