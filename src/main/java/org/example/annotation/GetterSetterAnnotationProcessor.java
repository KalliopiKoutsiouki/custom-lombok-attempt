package org.example.annotation;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.io.File;
@SupportedAnnotationTypes({"org.example.annotation.GetterMy", "org.example.annotation.SetterMy"})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class GetterSetterAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(GetterMy.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;

                String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
                String className = typeElement.getSimpleName().toString();
                String generatedClassName = className + "Generated";

                // Create a StringBuilder to accumulate the generated code
                StringBuilder generatedCode = new StringBuilder();
//                generatedCode.append("package ").append(packageName).append(";\n\n");
                generatedCode.append("public class ").append(generatedClassName).append(" {\n");

                for (Element enclosedElement : typeElement.getEnclosedElements()) {
                    if (enclosedElement.getKind() == ElementKind.FIELD) {
                        String fieldName = enclosedElement.getSimpleName().toString();
                        String fieldType = enclosedElement.asType().toString();
                        String fieldTypeTrimed = trimType(fieldType);
                        String variable = "\n   private " + fieldTypeTrimed + " " + fieldName + ";\n";
                        generatedCode.append(variable);
                    }
                }

                // Generate getters for all fields
                for (Element enclosedElement : typeElement.getEnclosedElements()) {
                    if (enclosedElement.getKind() == ElementKind.FIELD) {
                        String fieldName = enclosedElement.getSimpleName().toString();
                        String fieldType = enclosedElement.asType().toString();
                        String fieldTypeTrimed = trimType(fieldType);
                        String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                        // Generate the getter method
                        generatedCode.append("\n    public ").append(fieldTypeTrimed).append(" ").append(getterName).append("() {\n");
                        generatedCode.append("        return this.").append(fieldName).append(";\n");
                        generatedCode.append("    }\n");
                        // Generate the setter method
                        generatedCode.append("\n    public void ").append(setterName).append("(").append(fieldTypeTrimed).append(" ").append(fieldName).append(") {\n");
                        generatedCode.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
                        generatedCode.append("    }\n");
                    }
                }

                // Close the class
                generatedCode.append("}\n");

                // Define the output directory path for the generated source file
                String outputPath = "target/generated-sources/annotations/" + packageName.replace(".", "/");

                // Create the output directory if it doesn't exist
                new File(outputPath).mkdirs();

                // Write the generated code to a new Java source file
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
        }
        return true;
    }

    private static String trimType(String fieldType) {
        int lastIndex = fieldType.lastIndexOf('.');
        String fieldTypeTrimed = fieldType.substring(lastIndex + 1);
        return fieldTypeTrimed;
    }
}


//    @Override
//    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//        for (Element element : roundEnv.getElementsAnnotatedWith(GetterMy.class)) {
//            if (element.getKind() == ElementKind.FIELD) {
//                String fieldName = element.getSimpleName().toString();
//                String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
//                String fieldType = element.asType().toString();
//                int lastIndex = fieldType.lastIndexOf('.');
//                String fieldTypeTrimed = fieldType.substring(lastIndex + 1);
//
//                String variable = "private " + fieldTypeTrimed + " " + fieldName + ";\n";
//                // Generate the getter method
//                String getterMethod = "public " + fieldTypeTrimed + " " + getterName + "() {\n" +
//                        "    return this." + fieldName + ";\n" +
//                        "}\n";
//
//                String packageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
//
//                // Define the output directory path for the generated source file
//                String outputPath = "target/generated-sources/annotations/" + packageName.replace(".", "/");
//
//                // Create the output directory if it doesn't exist
//                new File(outputPath).mkdirs();
//
//                // Write the generated code to a new Java source file
//                try {
//                    JavaFileObject javaFileObject = processingEnv.getFiler().createSourceFile(
//                            packageName + "." + element.getEnclosingElement().getSimpleName() + "Generated");
//                    try (PrintWriter out = new PrintWriter(javaFileObject.openWriter())) {
//                        out.println("package " + packageName + ";"); // Write the package declaration
//                        out.println(); // Add a blank line
//                        out.println("public class " + element.getEnclosingElement().getSimpleName() + "Generated {");
//                        out.println(variable);
//                        out.println(getterMethod);
//                        out.println("}");
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return true;
//    }
//}

