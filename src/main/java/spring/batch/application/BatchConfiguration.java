package spring.batch.application;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;

import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSetMapper;

import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;


import javax.sql.DataSource;
import java.util.List;

@Data

@Configuration
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener, Step step1) {
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1(JdbcBatchItemWriter<Person> writer) {
        return stepBuilderFactory.get("step1")
                .<Person, Person> chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer)
                .build();
    }

    @Bean
    public Step step1(JdbcBatchItemWriter<Person> writer) {
        return stepBuilderFactory.get("step1")
                .<Person, Person> chunk(10)
                .reader(reader())

                .processor(processor())
                .writer(writer)
                .build();
    }

    @Bean
    public FlatFileItemReader<Person> reader() {
        return new FlatFileItemReader<Person>()

                .setName("personItemReader")
                .setResource(new ClassPathResource("sample-data.csv"))
                .setLineMapper(new DefaultLineMapper<Person>() {{
                    setLineTokenizer(new DelimitedLineTokenizer() {{
                        setNames(new String[] {"firstName",
                                "lastName"}));
                    }});
                    setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                        setTargetType(Person.class);

                    }});
                }});
    }

    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    public static class PersonItemProcessor implements ItemProcessor<Person, Person> {

        @Override
        public Person process(final Person person) throws Exception
        {
            person.setFirstName(person.getFirstName().toUpperCase());
            person.setLastName(person.getLastName().toUpperCase());
            return person;

        }
    }

    @Bean
    public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Person>()

                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO people (first_name, last_name)
                        VALUES (:firstName, :lastName)")
                .dataSource(dataSource)

                .build();
    }

    
}
}
