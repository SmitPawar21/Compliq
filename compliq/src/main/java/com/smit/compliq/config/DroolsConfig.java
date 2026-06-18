package com.smit.compliq.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class DroolsConfig {
	private static final String RULES_PATH = "rules/";
    private static KieServices kieServices;
    
    public DroolsConfig() {
    	kieServices = KieServices.Factory.get();
    }

    @Bean
    public KieFileSystem kieFileSystem() {
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        // Load all .drl rule files from src/main/resources/rules/
        // Add more lines here if you have multiple rule files
        kieFileSystem.write(
            ResourceFactory.newClassPathResource(RULES_PATH + "compliance-rules.drl")
        );
        
        kieFileSystem.write(
            ResourceFactory.newClassPathResource(RULES_PATH + "invoice-rules.drl")
        );
        
        kieFileSystem.write(
            ResourceFactory.newClassPathResource(RULES_PATH + "contract-rules.drl")
        );

        return kieFileSystem;
    }

    @Bean
    public KieContainer kieContainer() {

        KieRepository kieRepository =
                kieServices.getRepository();

        kieRepository.addKieModule(
                () -> kieRepository.getDefaultReleaseId());

        KieBuilder kieBuilder =
                kieServices.newKieBuilder(
                        kieFileSystem());

        kieBuilder.buildAll();

        Results results =
                kieBuilder.getResults();

        if(results.hasMessages(Message.Level.ERROR)) {

            System.out.println("=== DROOLS ERRORS ===");

            results.getMessages(Message.Level.ERROR)
                    .forEach(System.out::println);

            throw new RuntimeException(
                    "Drools compilation failed");
        }

        return kieServices.newKieContainer(
                kieRepository.getDefaultReleaseId());
    }
  
    @Bean
    @Scope("prototype")
    public KieSession kieSession() {
        return kieContainer().newKieSession();
    }
}
