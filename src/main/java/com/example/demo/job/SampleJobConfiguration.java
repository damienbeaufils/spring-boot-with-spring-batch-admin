package com.example.demo.job;

import java.io.File;
import java.io.IOException;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class SampleJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final JobParametersIncrementer jobParametersIncrementer;

    @Value("classpath:work/records.csv")
    private Resource inputCsv;

    private Resource outputXml;

    @Autowired
    public SampleJobConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, JobParametersIncrementer jobParametersIncrementer) throws IOException {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobParametersIncrementer = jobParametersIncrementer;

        this.outputXml = new FileSystemResource(File.createTempFile("output", ".xml"));
    }

    @Bean
    public ItemReader<Transaction> itemReader() {
        String[] tokens = {"username", "userid", "transactiondate", "amount"};
        DefaultLineMapper<Transaction> lineMapper = lineMapper(tokens);

        FlatFileItemReader<Transaction> reader = new FlatFileItemReader<>();
        reader.setResource(inputCsv);
        reader.setLineMapper(lineMapper);
        return reader;
    }

    @Bean
    public ItemProcessor<Transaction, Transaction> itemProcessor() {
        return item -> item;
    }

    @Bean
    public ItemWriter<Transaction> itemWriter() {
        StaxEventItemWriter<Transaction> itemWriter = new StaxEventItemWriter<>();
        itemWriter.setMarshaller(marshaller());
        itemWriter.setRootTagName("transactionRecord");
        itemWriter.setResource(outputXml);
        return itemWriter;
    }

    @Bean
    public Step sampleStep(ItemReader<Transaction> reader, ItemProcessor<Transaction, Transaction> processor, ItemWriter<Transaction> writer) {
        return stepBuilderFactory.get("sampleStep").<Transaction, Transaction>chunk(10).reader(reader).processor(processor).writer(writer).build();
    }

    @Bean
    public JobExecutionListener jobExecutionListener() {
        return new JobExecutionListener() {

            @Override
            public void beforeJob(JobExecution jobExecution) {
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                try {
                    String exitDescription = "XML output has been written in this file: " + outputXml.getFile().getAbsolutePath();
                    jobExecution.setExitStatus(new ExitStatus(jobExecution.getExitStatus().getExitCode(), exitDescription));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Bean(name = "sampleBatchJob")
    public Job job(@Qualifier("sampleStep") Step sampleStep, JobExecutionListener jobExecutionListener) {
        return jobBuilderFactory.get("sampleBatchJob").incrementer(jobParametersIncrementer).flow(sampleStep).end().listener(jobExecutionListener).build();
    }

    private DefaultLineMapper<Transaction> lineMapper(String[] tokens) {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(tokens);

        DefaultLineMapper<Transaction> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new RecordFieldSetMapper());
        return lineMapper;
    }

    private Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Transaction.class);
        return marshaller;
    }
}