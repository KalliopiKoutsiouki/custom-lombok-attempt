# custom-lombok-attempt
Source code that can be installed in local maven repository and be used as dependency to offer similar functionality as Lombok.

1. clone the repo
2. mvn clean
3. mvn install (to install the .jar to your maven repository)
4. add it as a dependency to any maven project : 
        ```xml
              <dependency>
                  <groupId>org.example</groupId>
                  <artifactId>lombok_attempt</artifactId>
                  <version>1.0-SNAPSHOT</version>
              </dependency>


