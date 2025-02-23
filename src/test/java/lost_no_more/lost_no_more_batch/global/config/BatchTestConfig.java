package lost_no_more.lost_no_more_batch.global.config;

import javax.sql.DataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class BatchTestConfig {

    @Bean(name = "metaDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource-meta")
    public DataSource metaDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "dataDataSource")
    @ConfigurationProperties(prefix = "spring.datasource-data")
    public DataSource dataDataSource() {
        return DataSourceBuilder.create().build();
    }
}