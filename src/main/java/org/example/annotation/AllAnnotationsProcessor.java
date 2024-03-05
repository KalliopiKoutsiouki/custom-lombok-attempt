package org.example.annotation;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.io.File;

/**
 * Annotation processor that generates code for classes annotated with Getters, Setters, ArgsConstructor, and ToString.
 */
@SupportedAnnotationTypes({"org.example.annotation.Getters", "org.example.annotation.Setters", "org.example.annotation.ArgsConstructor"})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class AllAnnotationsProcessor extends AbstractProcessor {

    // Output directory path for generated source files
    private static final String OUTPUT_PATH_PREFIX = "target/generated-sources/annotations/";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // Iterate over each annotation and its elements
        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element.getKind() == ElementKind.CLASS) {
                    TypeElement typeElement = (TypeElement) element;
                    String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
                    String className = typeElement.getSimpleName().toString();
                    String generatedClassName = className + "Generated";
                    StringBuilder generatedCode = new StringBuilder();

                    // commenting out package creation for the specific use in the custom-orm-project
                    // generatedCode.append("package ").append(packageName).append(";\n\n");

                    // Class declaration
                    generatedCode.append("public class ").append(generatedClassName).append(" {\n");
                    List<String> constructorArgs = new ArrayList<>();

                    // Generate fields and populate constructor arguments
                    generateFieldsAndPopulateConstuctorArgs(typeElement, constructorArgs, generatedCode);

                    // Generate default constructor
                    generateDefaultConstructor(generatedCode, generatedClassName);

                    // Generate all-args constructor
                    if (isAllArgsConstrAnnotation(roundEnv)) {
                        generateAllArgsConstructor(generatedCode, generatedClassName, constructorArgs, typeElement);
                    }
                    // Generate getters and setters
                    if (isGetterAnnotation(roundEnv) || isSetterAnnotation(roundEnv)) {
                        generateGettersAndSetters(typeElement, generatedCode, roundEnv);
                    }
                    // Generate toString method if annotated with ToString
                    if (isToStringConstrAnnotation(roundEnv)){
                        generateToStringMethod(typeElement, generatedCode);
                    }
                    // Close the class
                    generatedCode.append("}\n");
                    // Define the output directory path for the generated source file
                    String outputPath = OUTPUT_PATH_PREFIX + packageName.replace(".", "/");
                    // Create the output directory if it doesn't exist
                    new File(outputPath).mkdirs();
                    // Write the generated code to a new Java source file
                    writeToJavaSourceFile(packageName, generatedClassName, generatedCode);
                }
            }
        }
        return true;
    }


    // Method to write the generated code to a Java source file
    private void writeToJavaSourceFile(String packageName, String generatedClassName, StringBuilder generatedCode) {
        try {
            JavaFileObject javaFileObject = processingEnv.getFiler().createSourceFile(
                    packageName + "." + generatedClassName);
            try (PrintWriter out = new PrintWriter(javaFileObject.openWriter())) {
                out.println(generatedCode.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to generate getters and setters
    private static void generateGettersAndSetters(TypeElement typeElement, StringBuilder generatedCode, RoundEnvironment roundEnv) {
        for (Element enclosedElement : typeElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.FIELD) {
                String fieldName = enclosedElement.getSimpleName().toString();
                String fieldType = enclosedElement.asType().toString();
                String fieldTypeTrimed = trimType(fieldType);
                String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                if(isGetterAnnotation(roundEnv)) {
                    generatedCode.append("\n    public ").append(fieldTypeTrimed).append(" ").append(getterName).append("() {\n");
                    generatedCode.append("        return this.").append(fieldName).append(";\n");
                    generatedCode.append("    }\n");
                }
                if (isSetterAnnotation(roundEnv)) {
                    generatedCode.append("\n    public void ").append(setterName).append("(").append(fieldTypeTrimed).append(" ").append(fieldName).append(") {\n");
                    generatedCode.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
                    generatedCode.append("    }\n");
                }
            }
        }
    }

    // Method to generate all-args constructor
    private static void generateAllArgsConstructor(StringBuilder generatedCode, String generatedClassName, List<String> constructorArgs, TypeElement typeElement) {
        generatedCode.append("\n    public ").append(generatedClassName).append("(");
        generatedCode.append(String.join(", ", constructorArgs));
        generatedCode.append(") {\n");

        // Populate fields in the constructor
        for (Element enclosedElement : typeElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.FIELD) {
                String fieldName = enclosedElement.getSimpleName().toString();
                generatedCode.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
            }
        }
        generatedCode.append("    }\n");
    }

    // Method to generate default constructor
    private static void generateDefaultConstructor(StringBuilder generatedCode, String generatedClassName) {
        generatedCode.append("\n    public ").append(generatedClassName).append("(){};\n");
    }

    // Method to generate fields and populate constructor arguments
    private static void generateFieldsAndPopulateConstuctorArgs(TypeElement typeElement, List<String> constructorArgs, StringBuilder generatedCode) {
        for (Element enclosedElement : typeElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.FIELD) {
                String fieldName = enclosedElement.getSimpleName().toString();
                String fieldType = enclosedElement.asType().toString();
                String fieldTypeTrimed = trimType(fieldType);
                String variable = "\n   private " + fieldTypeTrimed + " " + fieldName + ";\n";
                constructorArgs.add(fieldTypeTrimed + " " + fieldName);
                generatedCode.append(variable);
            }
        }
    }

    // Method to generate toString method
    private static void generateToStringMethod(TypeElement typeElement, StringBuilder generatedCode) {
        generatedCode.append("\n    @Override");
        generatedCode.append("\n    public String toString() {\n");
        generatedCode.append("        return \"").append(typeElement.getSimpleName()).append("{\" +\n");

        // Generate the toString method body by concatenating field names and values
        List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
        for (int i = 0; i < enclosedElements.size(); i++) {
            Element enclosedElement = enclosedElements.get(i);
            if (enclosedElement.getKind() == ElementKind.FIELD) {
                String fieldName = enclosedElement.getSimpleName().toString();
                generatedCode.append("                \"").append(fieldName).append("=\" + ").append(fieldName);
                if (i < enclosedElements.size()-5) {
                    generatedCode.append(" + \", \" + ");
                } else {
                    generatedCode.append(" + ");
                }
                generatedCode.append("\n");
            }
        }

        // Add the closing brace and semicolon to complete the toString method
        generatedCode.append("                '}';\n");
        generatedCode.append("    }\n");
    }

    private static String trimType(String fieldType) {
        int lastIndex = fieldType.lastIndexOf('.');
        String fieldTypeTrimed = fieldType.substring(lastIndex + 1);
        return fieldTypeTrimed;
    }

    private static boolean isGetterAnnotation(RoundEnvironment roundEnv) {
       Set<Element> elements = (Set<Element>) roundEnv.getElementsAnnotatedWith(Getters.class);
        return !elements.isEmpty();
    }

    private static boolean isSetterAnnotation(RoundEnvironment roundEnv) {
        Set<Element> elements = (Set<Element>) roundEnv.getElementsAnnotatedWith(Setters.class);
        return !elements.isEmpty();
    }

    private boolean isAllArgsConstrAnnotation(RoundEnvironment roundEnv) {
        Set<Element> elements = (Set<Element>) roundEnv.getElementsAnnotatedWith(ArgsConstructor.class);
        return !elements.isEmpty();
    }

    private boolean isToStringConstrAnnotation(RoundEnvironment roundEnv) {
        Set<Element> elements = (Set<Element>) roundEnv.getElementsAnnotatedWith(ToString.class);
        return !elements.isEmpty();
    }
}



